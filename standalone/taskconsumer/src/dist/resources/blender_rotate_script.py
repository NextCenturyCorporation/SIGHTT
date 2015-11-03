# ************************************************************************
#  Copyright (c), 2014 Next Century Corporation. All Rights Reserved.
#
#  This software was developed under the DARPA Visual Media Reasoning
#  project.
#
#                 Next Century Corporation
#             7075 Samuel Morse Drive, Ste 250
#                   Columbia, MD 21046
#                     (443) 545-3100
# ************************************************************************

import bpy
import bmesh
import random
import mathutils
from math import radians, pi
import math

# Blender rotate script 
# 
# The blender rotate script is used in conjunction with TaskConsumer and several
# files, including a model file and a background.   The rotate script will rotate 
# the object, render, and put it into the background in the right place at the right
# scale.

# ----------------------------------------------
# Passed parameters, set by the TaskConsumer
# ----------------------------------------------
# Output base directory, like '/tmp/AK47_23_145_35/'
output_dir = "${outputDir}"
# Full image path for the background object, like '/tmp/back/back_29C49B9AF.png'
background_path = "${backgroundPath}"
# Location for the object
pointX = ${modelPointX}
pointY = ${modelPointY}
# Amount to rotate the object about each axis
yaw = ${yaw}
pitch = ${pitch}
roll = ${roll}
# How much to scale the object.  >1 makes it bigger
scale_factor = ${scaleFactor}
# Whether to do plots of all layers 
all_layers = ${allLayers}

#Lighting
custom_lighting_model = ${useLightingModel}
directional_location = ${dirLocation}
directional_intensity = ${dirIntensity}
directional_color = ${dirColor}
use_ambient = ${useAmbient}
ambient_intensity = ${ambIntensity}
ambient_samples = ${ambSamples}

#GroundPlane
custom_ground_plane = ${useGroundPlaneModel}
ground_pos_x = ${groundPositionX}
ground_pos_y = ${groundPositionY}
ground_pos_z = ${groundPositionZ}
ground_rot_x = ${groundRotationX}
ground_rot_y = ${groundRotationY}

# ----------------------------------------------
# Calculated params from the passed
# ----------------------------------------------
output_path = output_dir + "composite.png"
# The output file node wants a directory (output_dir) and a filename (mask_file), rather than the entire filepath
mask_file = "compositemask.png"

# Intensities from the lighting are [0,1] but we need them [0,10]
directional_intensity *= 2
ambient_intensity *= 10

# -----------------------------------------------
# Initialize needed values
# -----------------------------------------------

# Initialize random with seed based on current time
random.seed()

# Size of the ground plane.  Make it big.  It just needs shadows, but can still need to be large if 
# the light angle is low
groundPlaneSize = 200

# Where to place the nodes (columns); rows are set in individual objects
col1 = 0
col2 = 200
col3 = 400
col4 = 600
col5 = 800

# -----------------------------------------------
# Functions
# -----------------------------------------------
def addNode(type, locx, locy):
    tree = bpy.context.scene.node_tree
    node = tree.nodes.new(type)
    node.location = locx, locy
    return node

def addLink(inp, out):
    tree = bpy.context.scene.node_tree
    link = tree.links.new(inp, out)
    return link

def setupNodes():
    print('Setting up compositor')
    bpy.context.scene.use_nodes = True
    tree = bpy.context.scene.node_tree
    background = bpy.data.images.load(background_path)
    
    # clear default nodes
    for n in tree.nodes:
        tree.nodes.remove(n)
    
    # -------------------
    # add nodes
    # -------------------
    
    # Render Layer
    rl = addNode('R_LAYERS', col1, 0)

    # Background
    im = addNode('IMAGE',  col1, 300)
    im.image = background

    # Alpha Overlay
    ao = addNode('ALPHAOVER', col4, 300)

    # Composite Node
    comp = addNode('COMPOSITE', col5, 300)

    # Render Mask Output
    global mask
    mask = addNode('OUTPUT_FILE', col4, 0)
    mask.base_path = output_dir
    mask.file_slots[0].path = mask_file

    # Scale/transform pipeline for background
    scale1 = addNode('SCALE', col2, 50)
    trans1 = addNode('TRANSLATE', col3, 50)

    # Scale/transform pipeline for render mask
    scale2 = addNode('SCALE', col2, -100)
    trans2 = addNode('TRANSLATE', col3, -100)

    # Set up scale/translation values
    scale1.inputs[1].default_value = scale_factor
    scale1.inputs[2].default_value = scale_factor
    scale2.inputs[1].default_value = scale_factor
    scale2.inputs[2].default_value = scale_factor

    trans1.inputs[1].default_value = pointX
    trans1.inputs[2].default_value = pointY
    trans2.inputs[1].default_value = pointX
    trans2.inputs[2].default_value = pointY
        
    # Create links between nodes
    links = tree.links
    link1 = addLink(im.outputs[0], ao.inputs[1])
    link2 = addLink(ao.outputs[0], comp.inputs[0])
    link3 = addLink(rl.outputs[0], scale1.inputs[0])
    link4 = addLink(rl.outputs[1], scale2.inputs[0])
    link5 = addLink(scale1.outputs[0], trans1.inputs[0])
    link6 = addLink(scale2.outputs[0], trans2.inputs[0])
    link7 = addLink(trans1.outputs[0], ao.inputs[2])
    link8 = addLink(trans2.outputs[0], mask.inputs[0])
    link9 = addLink(trans2.outputs[0], ao.inputs[0])
    render = bpy.data.scenes[0].render
    render.use_compositing = True
    render.resolution_x = background.size[0]
    render.resolution_y = background.size[1]
    render.resolution_percentage = 100


# Go through all the objects in a scene, determine what layer they
# are in, make a map of those layers to objects
def getActiveLayersMap(scene):
    layerToNameMap = dict()
    numLayers = len(scene.layers)
    for obj in scene.objects:
        for layerNum in range(0, numLayers):
            if (obj.layers[layerNum]):
                layerToNameMap[layerNum]=obj.name
    return layerToNameMap

# Turn on all the layers
def turnOnAllLayers(scene):
    layerFlags = []
    for layerNum in range(0,20):
        layerFlags.append(True)
    scene.render.layers[0].layers = layerFlags
    
# Turn off all the layers except for the passed onLayer.  This is a little tricky, because
# Blender will not turn off all layers, so you can't just turn off layer 0 and then layer 1, 
# because it will not turn off Layer 0, and you will end up with 0 and 1 on.  
def turnOnOnlyLayer(scene, onLayer):
    layerFlags = []
    for layerNum in range(0,20):
        layerFlags.append(False)
    layerFlags[onLayer] = True
    scene.render.layers[0].layers = layerFlags
    print ("Turned on layer ", onLayer)

# Place a sun at location and set its intensity and color.
def set_directional_lighting(location, intensity, color):
    for obj in bpy.data.objects:
        if obj.type == "LAMP":
            obj.hide_render = True
            
    sun = bpy.data.lamps.new("sightt_directional","SUN")
    sunobj = bpy.data.objects.new("sightt_directional",sun)
    bpy.context.scene.objects.link(sunobj)
    
    sunobj = bpy.data.objects['sightt_directional']
    sunobj.rotation_euler=((0,0,0))
    sunobj.location=(location)
    sun.shadow_method="RAY_SHADOW"
    
    bpy.data.lamps['sightt_directional'].energy = intensity
    
    for obj in bpy.data.objects:
        obj.select=False
        
    sunobj.select = True
    snorm = sunobj.location.normalized()
    pnorm = mathutils.Vector((0,0,1))
    rotaxis = snorm.cross(pnorm)
    top = (pnorm.x*snorm.x+pnorm.y*snorm.y+pnorm.z*snorm.z)
    bot = (pnorm.magnitude*snorm.magnitude)
    
    angle = math.acos(top/bot)
    
    # angle is negative because we want to rotate towards the origin.
    bpy.ops.transform.rotate(value=(-angle), axis=rotaxis)

# Set the ambient lighting on the object to ambientIntensity.
def set_ambient_lighting(cameraToUse, intensity):

    ambientLight = bpy.data.lamps.new("ambientLight","POINT")
    ambientLightObj = bpy.data.objects.new("ambientLight",ambientLight)
    bpy.context.scene.objects.link(ambientLightObj)

    # Put the a fill light at the camera location
    cam_loc = cameraToUse.location
    ambientLightObj = bpy.data.objects['ambientLight']
    ambientLightObj.location = cam_loc

    # Make the intensity the passed in value
    bpy.data.lamps['ambientLight'].energy = intensity

# Set the ground plane.
def set_ground_plane(rotx, roty):

    print('Applying custom ground plane')

    bpy.ops.mesh.primitive_plane_add(location=(0,0,0))
    groundPlane = bpy.context.object
    groundPlane.scale = (groundPlaneSize,groundPlaneSize,groundPlaneSize)

    mat=bpy.data.materials.new('planeMat')
    mat.use_only_shadow = True
    mat.shadow_only_type='SHADOW_ONLY'
    mat.use_transparency = True
    mat.transparency_method = 'Z_TRANSPARENCY'
    mat.alpha = 0.8
    groundPlane.data.materials.append(mat)

    # Rotate the plane so that it is vertical, so that our axis match up
    bpy.ops.transform.rotate(value=(1.57), axis=(1,0,0))

    # Now rotate according to passed values, with passed XY becoming XZ,
    # but you have to do the the Z first
    bpy.ops.transform.rotate(value=(ground_rot_y), axis=(0,0,1))
    bpy.ops.transform.rotate(value=(ground_rot_x), axis=(1,0,0))

    return groundPlane

def get_lowest_world_co_from_mesh(ob, point, normal, mat_parent=None):
    bme = bmesh.new()
    bme.from_mesh(ob.data)
    mat_to_world = ob.matrix_world.copy()
    if mat_parent:
        mat_to_world = mat_parent * mat_to_world
    lowest_co=None
    lowest_dist = 0.0
  
    for v in bme.verts:
        vmat = mat_to_world * v.co
        dist = mathutils.geometry.distance_point_to_plane(vmat, point, normal)
        if not lowest_co:
            lowest_co = vmat
        if (dist < lowest_dist):
            lowest_co = vmat
            lowest_dist = dist

    bme.free()
    print (" lowest coordinate: ", lowest_co)
    return lowest_co

def get_lowest_co_from_scene(name, point, normal):
    scene = bpy.context.scene
    lowest_co = None
    for ob in scene.objects:
        if (ob.type == 'MESH') and (ob.name != name):
            lowest_ob_l = get_lowest_world_co_from_mesh(ob,point,normal)
            if not lowest_co:
                lowest_co = lowest_ob_l
            if lowest_ob_l.z < lowest_co.z:
                lowest_co = lowest_ob_l
    print ("global lowest coordinate: ", lowest_co)
    return lowest_co

# --------------------------------------------------------------------
# Main program 
# --------------------------------------------------------------------

# Get the active scene.  Note:  The assumption is that there is one scene
# and it is active
scene = bpy.context.scene

# Get the active layers for the scene from the layer:name map.  set() gets unique values from the keys
layerToNameMap = getActiveLayersMap(scene)
activeLayers = set(layerToNameMap.keys())

# Set up the Node Compositor
setupNodes()    

#Select RGBA color scheme and PNG output...
scene.render.image_settings.file_format='PNG'
scene.render.image_settings.color_mode='RGBA'

# ----------------------------------------------------------------------
# Rotate the object:  Note:  Assumption that there is one object, and it is active
# ----------------------------------------------------------------------
"""
YAW = rotation around Z axis (negated because we are emulating the CAMERA rotating)
PITCH = rotation about Y axis
ROLL = rotation about CAMERA axis...
"""

# Correct for the difference between three.js coordinate system and blender coordinate system
# that we have decided upon.  
yaw += radians(90)

#Subtract the initial rotation of the object (points on sphere expects 0,0,0 rotation to be the front of the object, not the side.
yaw = -1*yaw

obj = bpy.context.object
objloc = obj.location.copy()
orig_rot = obj.rotation_euler.copy()
print ("ORIG ROT: ",orig_rot)

bpy.ops.transform.rotate(value=yaw,axis=(0,0,1))
delta_rot = obj.rotation_euler.copy()
print("NEW ROT: ",delta_rot)
delta_rot.x -= orig_rot.x
delta_rot.y -= orig_rot.y
delta_rot.z -= orig_rot.z
print ("DELTA ROT: ",delta_rot)
# We need to adjust our pitch rotation about global X by the same amount we rotated the object about global Z
xaxis=mathutils.Matrix.Rotation(yaw,4,"Z") * mathutils.Vector((1,0,0))
print("Xaxis:", xaxis)
bpy.ops.transform.rotate(value=pitch,axis=xaxis)

# Get the camera.  NOTE:  Assumption is that at least one camera exists
for obs in scene.objects:
    if (obs.type == 'CAMERA'):
        cameraToUse = obs

#obtain camera's Z axis, which is its 'view' axis
camaxis = cameraToUse.matrix_world * mathutils.Vector((0,0,1))
camaxis.normalize()
#This is to fix the rotation direction...
#IT IS ALREADY BEING DONE
#camaxis = camaxis * -1
print("Cam View Axis: ",camaxis)
bpy.ops.transform.rotate(value=roll,axis=camaxis)

# ----------------------------------------------------------------------
# Setup lighting
# ----------------------------------------------------------------------

if (custom_lighting_model == True):
    set_directional_lighting(directional_location,directional_intensity,directional_color)
    set_ambient_lighting(cameraToUse, ambient_intensity)
else:
    print('No custom lighting')

# ----------------------------------------------------------------------
# Set up Ground plane
# ----------------------------------------------------------------------

if (custom_ground_plane == True):
    groundPlane = set_ground_plane(ground_rot_x, ground_rot_y)
          
    # Get the lowest coordinate
    groundPlanePoint = groundPlane.matrix_world * groundPlane.data.vertices[0].co
    groundPlaneNormal = groundPlane.matrix_world * groundPlane.data.polygons[0].normal
    coord = get_lowest_co_from_scene(groundPlane.name, groundPlanePoint, groundPlaneNormal)

    groundPlane.location = coord
    
else:
    print('No custom ground plane')




# ----------------------------------------------------------------------
# Start rendering
# ----------------------------------------------------------------------

print('Job starting. Number of layers: ', len(activeLayers), ' To do all layers: ', all_layers)

# Tell Blender to render;  it will use the Nodes Compositor
turnOnAllLayers(scene)
scene.render.filepath = output_path
data_context = {"blend_data": bpy.context.blend_data, "scene": scene}
bpy.ops.render.render(data_context, write_still=True)

print('Finished entire Scene: ' + scene.name + ".  Mask at " + mask_file)

# Turn the layers on one at a time and render                                                                                                                             
if (all_layers and len(activeLayers) > 1):
    for rLayer in activeLayers:
        turnOnOnlyLayer(scene, rLayer)
        output_path = output_dir + "composite_"+layerToNameMap[rLayer]+".png"
        scene.render.filepath = output_path
        mask.file_slots[0].path = "mask_"+layerToNameMap[rLayer]+".png"
        data_context = {"blend_data": bpy.context.blend_data, "scene": bpy.context.scene}
        bpy.ops.render.render(data_context, write_still=True)

print (' Done render and composition. ')

