package jp.toastkid.web.video.seek

class BackwardSeek {

    fun makeScript(offsetFromLiveEdge: Int) = """
        (function() {
            var player = videojs.getPlayer(document.querySelector('.video-js'));
            if (!player && typeof videojs !== 'undefined' && videojs.getPlayers) {
                var players = videojs.getPlayers();
                var keys = Object.keys(players);
                if (keys.length > 0) {
                    player = players[keys[0]];
                }
            }
            
            // seekable レンジを取得（Video.js または HTML5 videoタグ）
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
            
            // ストリーミングのシーク可能範囲の端を取得
            var startTime = seekableRange.start(0); // シーク可能な最も古い時間（バッファの起点）
            var endTime = seekableRange.end(0);     // シーク可能な最も新しい時間（現在のライブ端）
            
            // ライブ端から指定秒数巻き戻した位置を算出
            var targetTime = endTime - $offsetFromLiveEdge;
            
            // 範囲内にクランプ（起点より古くならないようにする）
            if (targetTime < startTime) {
                targetTime = startTime;
            }
            
            // シークを実行
            if (player) {
                player.currentTime(targetTime);
            } else {
                document.querySelector('video').currentTime = targetTime;
            }
            
            return 'SUCCESS_SEEK: target=' + targetTime + ' (range: ' + startTime + ' ~ ' + endTime + ')';
        })();
    """.trimIndent()

    fun isTarget(urlString: String?): Boolean {
        return urlString?.contains("stockvoice.tv") == true
    }

}