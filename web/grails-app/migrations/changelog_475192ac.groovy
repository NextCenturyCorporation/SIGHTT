databaseChangeLog = {

	changeSet(author: "abovill (generated)", id: "1403116138706-1") {
		addColumn(tableName: "job") {
			column(name: "config_custom_cameras", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-2") {
		addColumn(tableName: "job") {
			column(name: "config_custom_ground_plane", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-3") {
		addColumn(tableName: "job") {
			column(name: "config_custom_lighting", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-4") {
		addColumn(tableName: "job") {
			column(name: "config_ground_planejson", type: "longtext") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-5") {
		addColumn(tableName: "job") {
			column(name: "config_lightingjson", type: "longtext") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-6") {
		addColumn(tableName: "job_config") {
			column(name: "custom_cameras", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-7") {
		addColumn(tableName: "job_config") {
			column(name: "custom_ground_plane", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-8") {
		addColumn(tableName: "job_config") {
			column(name: "custom_lighting", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-9") {
		addColumn(tableName: "job_config") {
			column(name: "ground_planejson", type: "longtext") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-10") {
		addColumn(tableName: "job_config") {
			column(name: "lightingjson", type: "longtext") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-11") {
		addColumn(tableName: "job_task") {
			column(name: "ambient_intensity", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-12") {
		addColumn(tableName: "job_task") {
			column(name: "ambient_samples", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-13") {
		addColumn(tableName: "job_task") {
			column(name: "ground_plane_positionx", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-14") {
		addColumn(tableName: "job_task") {
			column(name: "ground_plane_positiony", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-15") {
		addColumn(tableName: "job_task") {
			column(name: "ground_plane_positionz", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-16") {
		addColumn(tableName: "job_task") {
			column(name: "ground_plane_rotationx", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-17") {
		addColumn(tableName: "job_task") {
			column(name: "ground_plane_rotationy", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-18") {
		addColumn(tableName: "job_task") {
			column(name: "sun_color", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-19") {
		addColumn(tableName: "job_task") {
			column(name: "sun_intensity", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-20") {
		addColumn(tableName: "job_task") {
			column(name: "sun_location", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-21") {
		addColumn(tableName: "job_task") {
			column(name: "use_ambient", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-22") {
		addColumn(tableName: "job_task") {
			column(name: "use_ground_plane_model", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-23") {
		addColumn(tableName: "job_task") {
			column(name: "use_lighting_model", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-24") {
		modifyDataType(columnName: "config_active_camerasjson", newDataType: "longtext", tableName: "job")
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-25") {
		addNotNullConstraint(columnDataType: "longtext", columnName: "config_active_camerasjson", tableName: "job")
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-26") {
		modifyDataType(columnName: "active_camerasjson", newDataType: "longtext", tableName: "job_config")
	}

	changeSet(author: "abovill (generated)", id: "1403116138706-27") {
		addNotNullConstraint(columnDataType: "longtext", columnName: "active_camerasjson", tableName: "job_config")
	}
}
