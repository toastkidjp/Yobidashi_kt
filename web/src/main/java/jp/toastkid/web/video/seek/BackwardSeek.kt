package jp.toastkid.web.video.seek

class BackwardSeek {

    fun makeBackwardSeekScript(offsetFromLiveEdge: Int) = makeScript(makeBackwardSeekSnippet(offsetFromLiveEdge))

    private fun makeBackwardSeekSnippet(offsetFromLiveEdge: Int) = """
            var startTime = seekableRange.start(0);
            var endTime = seekableRange.end(0);
            
            var targetTime = endTime - $offsetFromLiveEdge;
            
            if (targetTime < startTime) {
                targetTime = startTime;
            }        
    """.trimIndent()

    private fun makeScript(seekTimeSnippet: String) = """
        (function() {
            var player = videojs.getPlayer(document.querySelector('.video-js'));
            if (!player && typeof videojs !== 'undefined' && videojs.getPlayers) {
                var players = videojs.getPlayers();
                var keys = Object.keys(players);
                if (keys.length > 0) {
                    player = players[keys[0]];
                }
            }
            
            var seekableRange = null;
            if (player && typeof player.seekable === 'function') {
                seekableRange = player.seekable();
            } else {
                var videoTag = document.querySelector('video');
                if (videoTag && videoTag.seekable) {
                    seekableRange = videoTag.seekable;
                }
            }
            
            if (!seekableRange || seekableRange.length === 0) {
                return 'ERROR: NO_SEEKABLE_RANGE';
            }
            
            ${seekTimeSnippet}
            
            if (player) {
                player.currentTime(targetTime);
            } else {
                document.querySelector('video').currentTime = targetTime;
            }
            
            return 'SUCCESS_SEEK: target=' + targetTime;
        })();
    """.trimIndent()

    fun isTarget(urlString: String?): Boolean {
        return urlString?.contains("stockvoice.tv") == true
    }

}