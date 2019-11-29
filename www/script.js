window.onload = function(e) {
    console.log("Hello World !");
    var time = document.getElementById("time");
    var i = 0;
    setInterval(function() {
        time.textContent = (++i).toString();
    }, 1000)
};
