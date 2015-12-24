// Project: LinkRest


/*
 * Table of Contents
 *
 *  1. Global Functions and Vars
 *		1.1 Youtube players init
 *
 *  2. Document Ready
 *		2.1 Youtube Video
 *		2.2 Current year in footer
 *
 *  3. Additional Plugins
 */


//---------------------------------------------------------------------------
//  1. GLOBAL FUNCTIONS
//---------------------------------------------------------------------------


	// 1.1) Youtube players init
		
		//youtube player references once API is loaded
		var YTplayer, YTstartTime;

		// gets called once the player API has loaded
		// this func should be on upper level
		function onYouTubeIframeAPIReady() {
			// after the DOM is loaded
			$(document).ready(function() {
				
				var frameIDVal = $('.yt-player').attr('id');

				//each instance has the individual iframe id
				YTplayer = new YT.Player(frameIDVal, {
					events: {
						'onReady': onPlayerReady,
						'onStateChange': onPlayerStateChange
					}
				});
			});
		}

		// Fires when the player is ready
		function onPlayerReady(event) {
			YTstartTime = event.target.getCurrentTime();
		}

		// Fires when the player's state changes.
		function onPlayerStateChange(event) {
			// Go to the next video after the current one is finished playing
			if (event.data === 0) {
				// show splash-screen
				$('.splash-screen').removeClass('off');
				// seek to YTstartTime and stop playing
				if (YTstartTime !== 0) {
					YTplayer.seekTo(YTstartTime).stopVideo();
				}
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
		// Asynchronously load the Youtube Iframe API 
		// if (typeof(YT) === 'undefined' || typeof(YT.Player) === 'undefined') {
		var tag = document.createElement('script');
		tag.src = 'https://www.youtube.com/iframe_api';
		var firstScriptTag = document.getElementsByTagName('script')[0];
		firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
		// }
	}

	$('.play-btn').on('click', function() {
		// hide splash-screen
		$('.splash-screen').addClass('off');
		
		// `smart` .playVideo()
		// if there is previous `playback`-position
		if (YTstartTime !== 0) {
			YTplayer.seekTo(YTstartTime);
		}
		// play video
		YTplayer.playVideo();
	});


	// 2.2 Current year in footer
	var currentYear = new Date().getFullYear();
	$('.current-year').text(currentYear);

}); //end $(document).ready()