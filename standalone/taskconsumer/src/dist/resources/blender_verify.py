
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

#############################################################
# 
# Determine the validity of Blender models passed into SIGHTT
#
#############################################################

import bpy

class BlenderValidator:
    def __init__(self):

    def validateWorlds(self):
        if(len(bpy.data.worlds)!=1):
            raise InvalidBlenderFileError("Number of worlds should be 1")
    
    def validateCameras(self):
        if(len(bpy.data.cameras)!=1):
            raise InvalidBlenderFileError("Number of cameras should be 1")

    def validate(self):
        try:
            validateWorlds()
            validateCameras()
        except InvalidBlenderFileError:
            
            

def main():
    validator = BlenderValidator()
    validator.validate()

if __name__ == "__main__":
    main()
