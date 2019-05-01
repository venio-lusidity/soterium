(function ($) {
    var methods = {
        init: function (opts) {
            var options = opts ? opts : null;
            var filesUpload = this;
            options.filesUpload = filesUpload;
            options.dropArea = document.getElementById(options.dropArea);
            options.fileList = document.getElementById(options.fileList);

            function uploadFile(file, isFile) {
                if (isFile) {
                    var li = document.createElement("li"),
                        div = document.createElement("div"),
                        img,
                        progressBarContainer = document.createElement("div"),
                        progressBar = document.createElement("div"),
                        reader,
                        xhr,
                        fileInfo;

                    li.appendChild(div);

                    progressBarContainer.className = "process-bar-container";
                    progressBar.className = "process-bar";
                    progressBarContainer.appendChild(progressBar);
                    li.appendChild(progressBarContainer);

                    /*
                        If the file is an image and the web browser supports FileReader,
                        present a preview in the file list
                    */
                    if (window.FileReader && typeof FileReader !== "undefined" && (/image/i).test(file.type)) {
                        img = document.createElement("img");
                        li.appendChild(img);
                        reader = new FileReader();
                        reader.onload = (function (theImg) {
                            return function (evt) {
                                theImg.src = evt.target.result;
                            };
                        }(img));
                        reader.readAsDataURL(file);
                    }
                }

                if (options.onbeforesend) {
                    options.onbeforesend(options);
                }

                // Uploading - for Firefox, Google Chrome and Safari
                var xhr = new XMLHttpRequest();

                // Update process bar
                xhr.upload.addEventListener("progress", function (evt) {
                    if (evt.lengthComputable) {
                        progressBar.style.width = (evt.loaded / evt.total) * 100 + "%";
                    }
                    else {
                        // No data to calculate on
                    }
                }, false);

                // File uploaded
                xhr.addEventListener("load", function () {
                    progressBarContainer.className += " uploaded";
                    progressBar.innerHTML = "Uploaded!";
                }, false);

                /*
                    0: request not initialized 
                    1: server connection established
                    2: request received 
                    3: processing request 
                    4: request finished and response is ready
                */
                xhr.onreadystatechange = function () {
                    if (xhr.readyState == 4 && xhr.status == 200) {
                        if (options.onsuccess) {
                            options.onsuccess(xhr.responseText, options);
                        }
                    }
                    else if (xhr.readyState == 4) {
                        if (options.onerror) {
                            options.onerror(options);
                        }
                    }
                };

                xhr.open(options.methodType, options.url + '&file=true', true);

                // Set appropriate headers
                xhr.setRequestHeader("Content-Type", options.contentType);
                xhr.setRequestHeader("X-File-Name", file.name);
                xhr.setRequestHeader("X-File-Size", file.size);
                xhr.setRequestHeader("X-File-Type", file.type);

                // Send the file (doh)
                xhr.send(file);

                if (isFile) {
                    // Present file info and append it to the list of files
                    fileInfo = "<div><strong>Name:</strong> " + file.name + "</div>";
                    fileInfo += "<div><strong>Size:</strong> " + parseInt(file.size / 1024, 10) + " kb</div>";
                    fileInfo += "<div><strong>Type:</strong> " + file.type + "</div>";
                    div.innerHTML = fileInfo;
                }

                options.fileList.appendChild(li);
            }


            function traverseFiles(files) {
                if (typeof files !== "undefined") {
                    for (var i = 0, l = files.length; i < l; i++) {
                        uploadFile(files[i], true);
                    }
                }
            }

            $(filesUpload).bind("change", function (event) {
                event.preventDefault();
                event.stopPropagation();
                var files = this.files;
                if (typeof files !== "undefined") {
                    traverseFiles(this.files);
                    if (options.onchange) {
                        options.onchange(event, options);
                    }
                }
            });
            if (window.File && window.FileList && window.Blob && Modernizr.draganddrop) {
                options.dropArea.addEventListener("dragleave", function (evt) {
                    evt.preventDefault();
                    evt.stopPropagation();
                    var target = evt.target;                    
                    if (target && target === options.dropArea) {
                        if (options.ondragleave) {
                            options.ondragleave(evt, options);
                        }
                    }
                }, false);

                options.dropArea.addEventListener("dragenter", function (evt) {
                    evt.preventDefault();
                    evt.stopPropagation();
                    if (options.ondragenter) {
                        options.ondragenter(evt, options);
                    }
                }, false);

                options.dropArea.addEventListener("dragover", function (evt) {
                    evt.preventDefault();
                    evt.stopPropagation();
                    if (options.ondragover) {
                        options.ondragover(evt, options);
                    }
                }, false);

                options.dropArea.addEventListener("drop", function (evt) {
                    evt.preventDefault();
                    evt.stopPropagation();
                    if (options.ondrop) {
                        options.ondrop(evt, options);
                    }
                    traverseFiles(evt.dataTransfer.files);
                }, false);
            }
            else {
                if (options.onnosupport) {
                    options.onnosupport(options);
                }
            }
        }
    };
    $.fn.fileupload = function (method) {

        // Method calling logic
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist on jQuery.tooltip');
        }

    };
})(jQuery);