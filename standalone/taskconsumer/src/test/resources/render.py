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

print('Job starting')

scene_number = len(bpy.data.scenes)

for count, Scn in enumerate(bpy.data.scenes):
#here you can add some lines about things you might want to set across all scenes, such as layer visiblity etc. Otherwise they will all render with the unique set up each scene has.

    for yaw in range(0,24):

        bpy.ops.transform.rotate(value=(0.26,), axis=(-1, 0, 0))
    	
        for pitch in range(0,24):

            bpy.ops.transform.rotate(value=(0.26,), axis=(0, -1, 0))

            for roll in range(0,24):

                bpy.ops.transform.rotate(value=(0.26,), axis=(0, 0, -1))

                Scn.render.filepath = "/tmp/renders/image_"+str(yaw)+"_"+str(pitch)+"_"+str(roll)+".png"
                data_context = {"blend_data": bpy.context.blend_data, "scene": Scn}

                bpy.ops.render.render(data_context, write_still = True)
                print('Finished Scene: ' + Scn.name + '. File saved.')

print('Job finished')


