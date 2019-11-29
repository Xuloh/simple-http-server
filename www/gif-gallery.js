window.onload = () => {
    let form = document.getElementById("form");
    let fileInput = document.getElementById("file");
    let error = document.getElementById("error");
    form.onsubmit = e => {
        e.preventDefault();
        let file = fileInput.files[0];
        if(!file.type.match(/image\/gif/)) {
            error.textContent = "Come on this is not a gif";
        }
        else {
            error.textContent = "";
            let oReq = new XMLHttpRequest();
            oReq.open("POST", "/gif", true);
            // oReq.setRequestHeader("Content-Type", "image/gif");
            oReq.onload = () => {
                window.location.reload(true);
            };
            let reader = new FileReader();
            reader.readAsArrayBuffer(file);
            reader.onload = () => {
                let base64Data = _arrayBufferToBase64(reader.result);
                oReq.send(base64Data);
            };
        }
    }
};

function _arrayBufferToBase64( buffer ) {
    var binary = '';
    var bytes = new Uint8Array( buffer );
    var len = bytes.byteLength;
    for (var i = 0; i < len; i++) {
        binary += String.fromCharCode( bytes[ i ] );
    }
    return window.btoa( binary );
}
