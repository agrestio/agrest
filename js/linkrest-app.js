// Project: LinkRest


/*
 * Table of Contents
 *
 *  1. Global Functions and Vars
 *		1.1 Youtube players init
 *
 *  2. Document Ready
 *      2.1 Youtube Video
 *
 *  3. Additional Plugins
 */


//---------------------------------------------------------------------------
//  1. GLOBAL FUNCTIONS
//---------------------------------------------------------------------------


	// 1.1) Youtube players init
		
		//youtube player references once API is loaded
		var YTplayer, YTadditional;

		// gets called once the player API has loaded
		// this func should be on upper level
		function onYouTubeIframeAPIReady() {
			// after the DOM is loaded
			$(document).ready(function() {
				
				var frameIDVal = $('.yt-player').attr('id');

				//each instance has the individual iframe id
				YTplayer = new YT.Player(frameIDVal, {
					events: {
						'onStateChange': onPlayerStateChange
					}
				});

				// connect modalID and playerID
				YTadditional = {
					frameID: frameIDVal,
					currentPos: null
				};
				$.extend(YTplayer, YTadditional);
			
			});

		}

		// Fires when the player's state changes.
		function onPlayerStateChange(event) {
			// Go to the next video after the current one is finished playing
			if (event.data === 0) {
				$('.video-container-holder').removeClass('active');
				YTplayer.seekTo(23).stopVideo();

			}
		}
		// function onYouTubePlayerAPIReady() {
		// }



//---------------------------------------------------------------------------
//  2. DOCUMENT READY
//---------------------------------------------------------------------------

$(document).ready(function() {

	// 2.1 Youtube Video
	// if there are iframe YT players on page
	if ($('.yt-player').length) {
		// Asynchronously load the Youtubr Iframe API 
		// if (typeof(YT) === 'undefined' || typeof(YT.Player) === 'undefined') {
		var tag = document.createElement('script');
		tag.src = 'https://www.youtube.com/iframe_api';
		var firstScriptTag = document.getElementsByTagName('script')[0];
		firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
		// }
	}

	$('.play-btn').on('click', function() {
		// hide splash-screen
		$('.video-container-holder').addClass('active');
		
		// `smart` .playVideo()
		// if there is previous `playback`-position
		if (YTplayer.currentPos !== null) {
			YTplayer.seekTo(YTplayer.currentPos);
		}
		// play video
		YTplayer.playVideo();
	});

}); //end $(document).ready()