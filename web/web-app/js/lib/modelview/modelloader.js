/**
 * Created with IntelliJ IDEA.
 * User: abovill
 * Date: 1/14/14
 * Time: 5:00 PM
 * To change this template use File | Settings | File Templates.
 */

function log(msg) {
    if (window.console && window.console.log) {
        console.log(msg);
    }
}

ModelLoader = function (event) {
    // Code from https://developer.mozilla.org/En/Using_XMLHttpRequest#Receiving_binary_data
    this.load_binary_resource = function (url) {
        var req = new XMLHttpRequest();
        req.open('GET', url, false);
        // The following line says we want to receive data as Binary and not as Unicode
        req.overrideMimeType('text/plain; charset=x-user-defined');
        req.send(null);
        if (req.status != 200) {
            return '';
        }

        return req.responseText;
    };

    this.loadOBJ = function (url) {
        postMessage({'status': 'message', 'content': 'Downloading ' + url});
        var file = this.load_binary_resource(url);
        this.loadOBJString(file);
    };

    this.loadOBJString = function (OBJString) {
        postMessage({'status': 'message', 'content': 'Parsing OBJ String...'});
        postMessage({'status': 'complete', 'content': this.ParseOBJString(OBJString)});
    };

    this.ParseOBJString = function (OBJString) {
        var vertexes = [];
        var faces = [];

        var lines = OBJString.split("\n");

        // var normal_position = 0;

        for (var i = 0; i < lines.length; i++) {
            postMessage({'status': 'progress', 'content': parseInt(i / lines.length * 100) + '%'});

            line_parts = lines[i].replace(/\s+/g, " ").split(" ");

	    // check to ensure valid part, sometimes a 'face' can have 2 points
	    if (line_parts.length >= 4) {
                if (line_parts[0] == "v") {
                    vertexes.push([parseFloat(line_parts[1]), parseFloat(line_parts[2]), parseFloat(line_parts[3])]);
                } else if (line_parts[0] == "f") {

		    var faceVert = [];
		    for (var faceVertIndex = 1; faceVertIndex < line_parts.length; faceVertIndex++) {
			faceVert.push( parseFloat(line_parts[faceVertIndex].split("/")[0]) - 1) ;
		    }

		    // If there are 3 points, then it is a triangle, and so we just want the points 
		    if (faceVert.length == 3) {
			faces.push([faceVert[0], faceVert[1], faceVert[2],0]);
		    } 
		    // if 4 points, then it is a rectangle, where we create 2 triangle faces
		    else if (faceVert.length == 4) {
			faces.push([faceVert[0], faceVert[1], faceVert[2],0]);
			faces.push([faceVert[2], faceVert[3], faceVert[0],0]);
		    }
		    else {
			log(" Ack. odd # of vertices in a face");
			log("line: " + lines[i] + " parts[0] " + line_parts[0] );
		    }
                }
	    } 
        }

        return [vertexes, faces];
    };


    this.loadSTL = function (url) {
        var looksLikeBinary = function (reader) {
            // STL files don't specify a way to distinguish ASCII from binary.
            // The usual way is checking for "solid" at the start of the file --
            // but Thingiverse has seen at least one binary STL file in the wild
            // that breaks this.

            // The approach here is different: binary STL files contain a triangle
            // count early in the file.  If this correctly predicts the file's length,
            // it is most probably a binary STL file.

            reader.seek(80);  // skip the header
            var count = reader.readUInt32();

            var predictedSize = 80 /* header */ + 4 /* count */ + 50 * count;
            return reader.getSize() == predictedSize;
        };

        postMessage({'status': 'message', 'content': 'Downloading ' + url});
        var file = this.load_binary_resource(url);
        var reader = new BinaryReader(file);

        if (looksLikeBinary(reader)) {
            this.loadSTLBinary(reader);
        } else {
            this.loadSTLString(file);
        }
    };

    this.loadSTLString = function (STLString) {
        postMessage({'status': 'message', 'content': 'Parsing STL String...'});
        postMessage({'status': 'complete', 'content': this.ParseSTLString(STLString)});
    };

    this.loadSTLBinary = function (STLBinary) {
        postMessage({'status': 'message', 'content': 'Parsing STL Binary...'});
        postMessage({'status': 'complete', 'content': this.ParseSTLBinary(STLBinary)});
    };

    this.ParseSTLBinary = function (input) {
        // Skip the header.
        input.seek(80);

        // Load the number of vertices.
        var count = input.readUInt32();

        // During the parse loop we maintain the following data structures:
        var vertices = [];   // Append-only list of all unique vertices.
        var vert_hash = {};  // Mapping from vertex to index in 'vertices', above.
        var faces = [];   // List of triangle descriptions, each a three-element
        // list of indices in 'vertices', above.

        for (var i = 0; i < count; i++) {
            if (i % 100 == 0) {
                postMessage({
                    'status': 'message',
                    'content': 'Parsing ' + (i + 1) + ' of ' + count + ' polygons...'
                });
                postMessage({
                    'status': 'progress',
                    'content': parseInt(i / count * 100) + '%'
                });
            }

            // Skip the normal (3 single-precision floats)
            input.seek(input.getPosition() + 12);

            var face_indices = [];
            for (var x = 0; x < 3; x++) {
                var vertex = [input.readFloat(), input.readFloat(), input.readFloat()];

                var vertexIndex = vert_hash[vertex];
                if (vertexIndex == null) {
                    vertexIndex = vertices.length;
                    vertices.push(vertex);
                    vert_hash[vertex] = vertexIndex;
                }

                face_indices.push(vertexIndex);
            }
            faces.push(face_indices);

            // Skip the "attribute" field (unused in common models)
            input.readUInt16();
        }

        return [vertices, faces];
    };

    // build stl's vertex and face arrays
    this.ParseSTLString = function (STLString) {
        var vertexes = [];
        var faces = [];

        var face_vertexes = [];
        var vert_hash = {}

        // strip out extraneous stuff
        STLString = STLString.replace(/\r/, "\n");
        STLString = STLString.replace(/^solid[^\n]*/, "");
        STLString = STLString.replace(/\n/g, " ");
        STLString = STLString.replace(/facet normal /g, "");
        STLString = STLString.replace(/outer loop/g, "");
        STLString = STLString.replace(/vertex /g, "");
        STLString = STLString.replace(/endloop/g, "");
        STLString = STLString.replace(/endfacet/g, "");
        STLString = STLString.replace(/endsolid[^\n]*/, "");
        STLString = STLString.replace(/\s+/g, " ");
        STLString = STLString.replace(/^\s+/, "");

        var facet_count = 0;
        var block_start = 0;

        var points = STLString.split(" ");

        postMessage({'status': 'message', 'content': 'Parsing vertices...'});
        for (var i = 0; i < points.length / 12 - 1; i++) {
            if ((i % 100) == 0) {
                postMessage({'status': 'progress', 'content': parseInt(i / (points.length / 12 - 1) * 100) + '%'});
            }

            var face_indices = [];
            for (var x = 0; x < 3; x++) {
                var vertex = [parseFloat(points[block_start + x * 3 + 3]), parseFloat(points[block_start + x * 3 + 4]), parseFloat(points[block_start + x * 3 + 5])];

                var vertexIndex = vert_hash[vertex];
                if (vertexIndex == null) {
                    vertexIndex = vertexes.length;
                    vertexes.push(vertex);
                    vert_hash[vertex] = vertexIndex;
                }

                face_indices.push(vertexIndex);
            }
            faces.push(face_indices);

            block_start = block_start + 12;
        }

        return [vertexes, faces];
    };
    switch (event.data.cmd) {
        case "loadSTL":
            this.loadSTL(event.data.param);
            break;
        case "loadSTLString":
            this.loadSTLString(event.data.param);
            break;
        case "loadSTLBinary":
            this.loadSTLBinary(event.data.param);
            break;
        case "loadOBJ":
            this.loadOBJ(event.data.param);
            break;
        case "loadOBJString":
            this.loadOBJString(event.data.param);
            break;
        case "loadJSON":
            this.loadJSON(event.data.param);
            break;
        case "loadPLY":
            this.loadPLY(event.data.param);
            break;
        case "loadPLYString":
            this.loadPLYString(event.data.param);
            break;
        case "loadPLYBinary":
            this.loadPLYBinary(event.data.param);
            break;
        default:
            postMessage({'msg': "UNKNOWN CMD!"});
            break;
    }
};
importScripts('binaryReader.js');
postMessage({status: "message", content: "ModelLoaderFinished"});
onmessage = ModelLoader;
