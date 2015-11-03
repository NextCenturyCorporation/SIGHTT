
/*********************************************************************************************************
 * Software License Agreement (BSD License)
 * 
 * Copyright 2014 Next Century Corporation. All rights reserved.   
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ***********************************************************************************************************/
databaseChangeLog = {

    changeSet(author: "abovill (generated)", id: "1385146932744-1") {
        createTable(tableName: "background") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "backgroundPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "bucket_name", type: "varchar(255)")

            column(name: "file_path", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "height", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "permissions", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "thumbnail_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "width", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-2") {
        createTable(tableName: "composite_image") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "composite_imaPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "bucket_name", type: "varchar(255)")

            column(name: "file_path", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "height", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "thumbnail_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "valid", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "width", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-3") {
        createTable(tableName: "geometry") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "geometryPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "background_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "image_size_in_pixels_height", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "top_left_corner_x", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "top_left_corner_y", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "image_size_in_pixels_width", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "json", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "origin_x", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "origin_y", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-4") {
        createTable(tableName: "geometry_vector3d") {
            column(name: "geometry_vectors_id", type: "bigint")

            column(name: "vector3d_id", type: "bigint")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-5") {
        createTable(tableName: "job") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "jobPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "actual_end_date", type: "datetime")

            column(name: "config_degree_spacing", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "config_generate_all_masks", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "config_image_type", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "config_model_background_scale", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "config_num_images", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "config_points_string", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "config_position", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "config_reproducible", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "expected_end_date", type: "datetime")

            column(name: "job_name", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "num_complete", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "num_tasks", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "permissions", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "submit_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "sum_of_execution_time", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "sum_of_execution_time_squared", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "zip_file_path", type: "varchar(255)")

            column(name: "zip_file_size", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-6") {
        createTable(tableName: "job_config") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "job_configPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "degree_spacing", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "generate_all_masks", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "image_type", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "model_background_scale", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "num_images", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "points_string", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "position", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "reproducible", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-7") {
        createTable(tableName: "job_config_background") {
            column(name: "job_config_backgrounds_id", type: "bigint")

            column(name: "background_id", type: "bigint")

            column(name: "backgrounds_idx", type: "integer")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-8") {
        createTable(tableName: "job_config_object_model") {
            column(name: "job_config_object_models_id", type: "bigint")

            column(name: "object_model_id", type: "bigint")

            column(name: "object_models_idx", type: "integer")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-9") {
        createTable(tableName: "job_task") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "job_taskPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "background_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "completed", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "composite_image_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "error", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "execution_time_end", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "execution_time_start", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "exit_value", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "job_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "locationx", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "locationy", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "object_model_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "pitch", type: "float") {
                constraints(nullable: "false")
            }

            column(name: "roll", type: "float") {
                constraints(nullable: "false")
            }

            column(name: "running", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "scale_factor", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "stderr", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "stdout", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "task_number", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "task_time_end", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "task_time_start", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "yaw", type: "float") {
                constraints(nullable: "false")
            }

            column(name: "job_tasks_idx", type: "integer")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-10") {
        createTable(tableName: "object_model") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "object_modelPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "bucket_name", type: "varchar(255)")

            column(name: "file_path", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "image_file_path", type: "varchar(255)")

            column(name: "model_type", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "obj_file_path", type: "varchar(255)")

            column(name: "output_error", type: "longtext")

            column(name: "output_exit_value", type: "integer")

            column(name: "output_output_image_filename", type: "varchar(255)")

            column(name: "output_stderr", type: "longtext")

            column(name: "output_stdout", type: "longtext")

            column(name: "owner_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "permissions", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "render_height", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "render_width", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "size_in_meters", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "thumbnail_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-11") {
        createTable(tableName: "password_reset_request") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "password_resePK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "complete", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "expiry", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "reset_code", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-12") {
        createTable(tableName: "pending_user") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "pending_userPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "activation_code", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "expiry", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "password_hash", type: "varchar(1024)") {
                constraints(nullable: "false")
            }

            column(name: "password_salt", type: "blob") {
                constraints(nullable: "false")
            }

            column(name: "registration_ip", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "username", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-13") {
        createTable(tableName: "rendered_view") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "rendered_viewPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "bucket_name", type: "varchar(255)")

            column(name: "file_path", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "height", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "size_in_meters", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "width", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-14") {
        createTable(tableName: "role") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "rolePK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-15") {
        createTable(tableName: "role_permissions") {
            column(name: "role_id", type: "bigint")

            column(name: "permissions_string", type: "varchar(255)")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-16") {
        createTable(tableName: "thumbnail") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "thumbnailPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "bucket_name", type: "varchar(255)")

            column(name: "file_path", type: "varchar(255)")

            column(name: "height", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "valid", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "width", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-17") {
        createTable(tableName: "user") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "userPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "password_hash", type: "varchar(1024)") {
                constraints(nullable: "false")
            }

            column(name: "password_salt", type: "blob") {
                constraints(nullable: "false")
            }

            column(name: "preferences_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "username", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-18") {
        createTable(tableName: "user_permissions") {
            column(name: "user_id", type: "bigint")

            column(name: "permissions_string", type: "varchar(255)")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-19") {
        createTable(tableName: "user_preferences") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "user_preferenPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "default_privacy", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-20") {
        createTable(tableName: "user_preferences_allowed_communications") {
            column(name: "user_preferences_id", type: "bigint")

            column(name: "allowed_communications", type: "varchar(255)")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-21") {
        createTable(tableName: "user_roles") {
            column(name: "user_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "role_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-22") {
        createTable(tableName: "vector3d") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "vector3dPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "length_in_meters", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "length_in_pixels", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "slope", type: "double precision")

            column(name: "slope_infinite", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "x", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "y", type: "double precision") {
                constraints(nullable: "false")
            }

            column(name: "z", type: "double precision") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-23") {
        addPrimaryKey(columnNames: "user_id, role_id", tableName: "user_roles")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-46") {
        createIndex(indexName: "FKB098552E3DB1A650", tableName: "background") {
            column(name: "thumbnail_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-47") {
        createIndex(indexName: "FKB098552E43A96ECE", tableName: "background") {
            column(name: "owner_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-48") {
        createIndex(indexName: "file_path_uniq_1385146932626", tableName: "background", unique: "true") {
            column(name: "file_path")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-49") {
        createIndex(indexName: "FK4FB43DA33DB1A650", tableName: "composite_image") {
            column(name: "thumbnail_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-50") {
        createIndex(indexName: "FK6E080872B0F0D1A4", tableName: "geometry") {
            column(name: "background_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-51") {
        createIndex(indexName: "FK64C12C4161EECB0", tableName: "geometry_vector3d") {
            column(name: "geometry_vectors_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-52") {
        createIndex(indexName: "FK64C12C41EF365B41", tableName: "geometry_vector3d") {
            column(name: "vector3d_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-53") {
        createIndex(indexName: "FK19BBD43A96ECE", tableName: "job") {
            column(name: "owner_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-54") {
        createIndex(indexName: "FKCC21DA49B0F0D1A4", tableName: "job_config_background") {
            column(name: "background_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-55") {
        createIndex(indexName: "FK26F00084C714D4B1", tableName: "job_config_object_model") {
            column(name: "object_model_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-56") {
        createIndex(indexName: "FK9FBC20C76A4521F5", tableName: "job_task") {
            column(name: "composite_image_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-57") {
        createIndex(indexName: "FK9FBC20C79124B130", tableName: "job_task") {
            column(name: "job_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-58") {
        createIndex(indexName: "FK9FBC20C7B0F0D1A4", tableName: "job_task") {
            column(name: "background_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-59") {
        createIndex(indexName: "FK9FBC20C7C714D4B1", tableName: "job_task") {
            column(name: "object_model_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-60") {
        createIndex(indexName: "FKC7B356293DB1A650", tableName: "object_model") {
            column(name: "thumbnail_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-61") {
        createIndex(indexName: "FKC7B3562943A96ECE", tableName: "object_model") {
            column(name: "owner_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-62") {
        createIndex(indexName: "file_path_uniq_1385146932671", tableName: "object_model", unique: "true") {
            column(name: "file_path")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-63") {
        createIndex(indexName: "FK492958FBD7C2BEB6", tableName: "password_reset_request") {
            column(name: "user_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-64") {
        createIndex(indexName: "email_uniq_1385146932676", tableName: "pending_user", unique: "true") {
            column(name: "email")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-65") {
        createIndex(indexName: "username_uniq_1385146932677", tableName: "pending_user", unique: "true") {
            column(name: "username")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-66") {
        createIndex(indexName: "file_path_uniq_1385146932678", tableName: "rendered_view", unique: "true") {
            column(name: "file_path")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-67") {
        createIndex(indexName: "name_uniq_1385146932680", tableName: "role", unique: "true") {
            column(name: "name")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-68") {
        createIndex(indexName: "FKEAD9D23B3297FAD6", tableName: "role_permissions") {
            column(name: "role_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-69") {
        createIndex(indexName: "FK36EBCBBAE02793", tableName: "user") {
            column(name: "preferences_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-70") {
        createIndex(indexName: "email_uniq_1385146932684", tableName: "user", unique: "true") {
            column(name: "email")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-71") {
        createIndex(indexName: "username_uniq_1385146932685", tableName: "user", unique: "true") {
            column(name: "username")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-72") {
        createIndex(indexName: "FKE693E610D7C2BEB6", tableName: "user_permissions") {
            column(name: "user_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-73") {
        createIndex(indexName: "FK62F4CCEFA5AD3A87", tableName: "user_preferences_allowed_communications") {
            column(name: "user_preferences_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-74") {
        createIndex(indexName: "FK734299493297FAD6", tableName: "user_roles") {
            column(name: "role_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-75") {
        createIndex(indexName: "FK73429949D7C2BEB6", tableName: "user_roles") {
            column(name: "user_id")
        }
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-24") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "background", constraintName: "FKB098552E43A96ECE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-25") {
        addForeignKeyConstraint(baseColumnNames: "thumbnail_id", baseTableName: "background", constraintName: "FKB098552E3DB1A650", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "thumbnail", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-26") {
        addForeignKeyConstraint(baseColumnNames: "thumbnail_id", baseTableName: "composite_image", constraintName: "FK4FB43DA33DB1A650", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "thumbnail", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-27") {
        addForeignKeyConstraint(baseColumnNames: "background_id", baseTableName: "geometry", constraintName: "FK6E080872B0F0D1A4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "background", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-28") {
        addForeignKeyConstraint(baseColumnNames: "geometry_vectors_id", baseTableName: "geometry_vector3d", constraintName: "FK64C12C4161EECB0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "geometry", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-29") {
        addForeignKeyConstraint(baseColumnNames: "vector3d_id", baseTableName: "geometry_vector3d", constraintName: "FK64C12C41EF365B41", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "vector3d", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-30") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "job", constraintName: "FK19BBD43A96ECE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-31") {
        addForeignKeyConstraint(baseColumnNames: "background_id", baseTableName: "job_config_background", constraintName: "FKCC21DA49B0F0D1A4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "background", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-32") {
        addForeignKeyConstraint(baseColumnNames: "object_model_id", baseTableName: "job_config_object_model", constraintName: "FK26F00084C714D4B1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "object_model", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-33") {
        addForeignKeyConstraint(baseColumnNames: "background_id", baseTableName: "job_task", constraintName: "FK9FBC20C7B0F0D1A4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "background", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-34") {
        addForeignKeyConstraint(baseColumnNames: "composite_image_id", baseTableName: "job_task", constraintName: "FK9FBC20C76A4521F5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "composite_image", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-35") {
        addForeignKeyConstraint(baseColumnNames: "job_id", baseTableName: "job_task", constraintName: "FK9FBC20C79124B130", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-36") {
        addForeignKeyConstraint(baseColumnNames: "object_model_id", baseTableName: "job_task", constraintName: "FK9FBC20C7C714D4B1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "object_model", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-37") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "object_model", constraintName: "FKC7B3562943A96ECE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-38") {
        addForeignKeyConstraint(baseColumnNames: "thumbnail_id", baseTableName: "object_model", constraintName: "FKC7B356293DB1A650", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "thumbnail", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-39") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "password_reset_request", constraintName: "FK492958FBD7C2BEB6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-40") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "role_permissions", constraintName: "FKEAD9D23B3297FAD6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "role", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-41") {
        addForeignKeyConstraint(baseColumnNames: "preferences_id", baseTableName: "user", constraintName: "FK36EBCBBAE02793", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user_preferences", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-42") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_permissions", constraintName: "FKE693E610D7C2BEB6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-43") {
        addForeignKeyConstraint(baseColumnNames: "user_preferences_id", baseTableName: "user_preferences_allowed_communications", constraintName: "FK62F4CCEFA5AD3A87", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user_preferences", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-44") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_roles", constraintName: "FK734299493297FAD6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "role", referencesUniqueColumn: "false")
    }

    changeSet(author: "abovill (generated)", id: "1385146932744-45") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_roles", constraintName: "FK73429949D7C2BEB6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

	include file: 'changelog_03df5e4.groovy'

	include file: 'changelog_fe4226b.groovy'

	include file: 'changelog_cd9fd32b18.groovy'


	include file: 'changelog_475192ac.groovy'
}
