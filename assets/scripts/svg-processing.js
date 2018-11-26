// SVG SUPPORT
function supportsSVG() {
  return !! document.createElementNS && !! document.createElementNS('http://www.w3.org/2000/svg','svg').createSVGRect;  
}
if (supportsSVG()) {
  document.documentElement.className += ' svg';
} else {
  document.documentElement.className += ' no-svg';
  var imgs = document.getElementsByTagName('img');
  var dotSVG = /.*\.svg$/;
  for (var i = 0; i != imgs.length; ++i) {
    if(imgs[i].src.match(dotSVG)) {
      imgs[i].src = imgs[i].src.slice(0, -3) + 'png';
    }
  }
}

// Replace all SVG images (.svg-icon) with inline SVG
Array.prototype.forEach.call(
    document.querySelectorAll('img.svg-icon'),
    function(img){
        var imgID = img.id;
        var imgClass = img.className;
        var imgURL = img.src;
        fetch(imgURL).then(function(response) {
            return response.text();
        }).then(function(text){
            var parser = new DOMParser();
            var xmlDoc = parser.parseFromString(text, "text/xml");
            var svg = xmlDoc.getElementsByTagName('svg')[0];
            if(typeof imgID !== 'undefined') {
                svg.setAttribute('id', imgID);
            }
            if(typeof imgClass !== 'undefined') {
                svg.setAttribute('class', imgClass+' replaced-svg');
            }
            svg.removeAttribute('xmlns:a');
            if(!svg.getAttribute('viewBox') && svg.getAttribute('height') && svg.getAttribute('width')) {
                svg.setAttribute('viewBox', '0 0 ' + svg.getAttribute('height') + ' ' + svg.getAttribute('width'))
            }
            img.parentNode.replaceChild(svg, img);
        });
    }
);

// Hide main menu
$(document).on('click', function (ev) {
    // if need to exclude specific DOM els use `ev.target`
    $('.navbar-collapse').collapse('hide');
});

// Navbar-overlay-blur
$('#mainmenu').on('show.bs.collapse', function () {
    $('html').addClass('navbar-collapse-shown');
});
$('#mainmenu').on('hide.bs.collapse', function () {
    $('html').removeClass('navbar-collapse-shown');
});

// ---------------------------------------------------------------------------
// Youtube Api
// ---------------------------------------------------------------------------
// https://developers.google.com/youtube/iframe_api_reference

// global variable for the player
var player;

// this function gets called when API is ready to use
function onYouTubePlayerAPIReady() {
  // create the global player from the specific iframe (#video)
  player = new YT.Player('video', {
    events: {
      // call this function when player is ready to use
      'onReady': onPlayerReady
    }
  });
}

function onPlayerReady(event) {
  
  // bind events
  $('#videoModal').on('shown.bs.modal', function (e) {
    player.playVideo();
  });
  
  $('#videoModal').on('hide.bs.modal', function (e) {
    player.stopVideo();
  });
  
}

// Inject YouTube API script
var tag = document.createElement('script');
tag.src = "//www.youtube.com/player_api";
var firstScriptTag = document.getElementsByTagName('script')[0];
firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

//---------------------------------------------------------------------------
//  DOCUMENT READY
//---------------------------------------------------------------------------

$(document).ready(function() {

	// Current year in footer
	var currentYear = new Date().getFullYear();
	$('.current-year').text(currentYear);

}); //end $(document).ready()