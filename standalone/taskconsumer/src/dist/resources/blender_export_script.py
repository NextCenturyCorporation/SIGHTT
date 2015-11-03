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

# Blender export script 
# 
# The blender export script will export the scene in the given blender file as
# an object (.obj) file in the given output directory.

# File path for exporting the scene as an object (.obj) file
# Example:  /tmp/AK47_23_145_35/object.obj
object_file_path = "${outputDir}" + "object.obj"

print (' Exporting scene as object (.obj) file... ')
print ("Ensuring that exported file uses Z-up and -Y-forward.")
bpy.ops.export_scene.obj(
    filepath=object_file_path,
    use_materials=False,
    axis_up='Z',
    axis_forward='Y'
)

print (' Done export. ')
