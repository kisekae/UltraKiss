

function MIDIPlayer(source,onload) {
    
    var audioContext = null;
    var player = null;
    var reverberator = null;
    var songStart = 0;
    var input = null;
    var currentSongTime = 0;
    var nextStepTime = 0;
    var nextPositionTime = 0;
    var loadedsong = null;
    var stoppedsong = null;
    var stepDuration = 44 / 1000;
    var lastPosition = 0;
    var self=this;

    this.currentPosition = 0;
    this.duration=0;
    this.onload = onload;
    this.STOPPED = "stopped";
    this.PLAYING = "playing";
    this.PAUSED = "paused";

    this.debug = false;
    // autoReplay is a boolean that indicates if the song should be replayed after it ends
    this.autoReplay = false;
    // ontick is a function that is called every time the song advances
    this.ontick = null;
    // onend is a function that is called when the song ends
    this.onend = null;
    
    this.log=function(msg, extra){
        if (this.debug) console.log(msg,extra);
    }

    this.play =function() {
        if (!loadedsong && stoppedsong){
            loadedsong = stoppedsong;
        }
        if (loadedsong) {
            try {
                this.startPlay(loadedsong);
                if (this.state==this.PAUSED){
                    this.setPosition(lastPosition);
                }
                this.state = this.PLAYING;
            } catch (expt) {
                this.log('error ', expt);
            }
        }
    }
    this.pause=function(){
        if (loadedsong) {
            lastPosition=this.getPosition();
            this.log("Paused position",lastPosition);
            this.stop();
            currentSongTime = lastPosition;
            this.state = this.PAUSED;
        }
    }
    this.stop=function(){
        if (loadedsong) {
            this.log("Stop");
            player.cancelQueue(audioContext);
            songStart = 0;
            currentSongTime = 0;
            stoppedsong = loadedsong;
            loadedsong = null;
            this.state = this.STOPPED;
        }
    }
    this.getContext=function(){
        return player;
    }
    this.startPlay =function(song) {
        currentSongTime = 0;
        songStart = audioContext.currentTime;
        nextStepTime = audioContext.currentTime;
        
        this.tick(song, stepDuration);
    }

    this.tick=function(song, stepDuration) {
        if (audioContext.currentTime > nextStepTime - stepDuration) {
            this.sendNotes(song, songStart, currentSongTime, currentSongTime + stepDuration, audioContext, input, player);
            currentSongTime = currentSongTime + stepDuration;
            nextStepTime = nextStepTime + stepDuration;
            if (currentSongTime > song.duration) {
                this.log("End of song");
                if (this.onend && typeof(this.onend)=="function"){
                    this.onend(loadedsong);
                }
                if (this.autoReplay){
                    currentSongTime = currentSongTime - song.duration;
                    this.sendNotes(song, songStart, 0, currentSongTime, audioContext, input, player);
                    songStart = songStart + song.duration;   
                } else {
                    if (this.ontick && typeof(this.ontick)=="function") {
                        this.ontick(loadedsong,song.duration);
                    }
                    this.stop();
                    return;    
                }
            }
        }
        if (nextPositionTime < audioContext.currentTime) {
            this.currentPosition = currentSongTime;
            this.duration = song.duration;
            nextPositionTime = audioContext.currentTime + 3;
        }
        if (this.ontick && typeof(this.ontick)=="function") {
            this.ontick(loadedsong,currentSongTime);
        }
        window.requestAnimationFrame(function (t) {
            if (loadedsong){
                self.tick(loadedsong, stepDuration);
            }
        });
    }
    this.sendNotes=function(song, songStart, start, end, audioContext, input, player) {
        for (var t = 0; t < song.tracks.length; t++) {
            var track = song.tracks[t];
            for (var i = 0; i < track.notes.length; i++) {
                if (track.notes[i].when >= start && track.notes[i].when < end) {
                    var when = songStart + track.notes[i].when;
                    var duration = track.notes[i].duration;
                    if (duration > 3) {
                        duration = 3;
                    }
                    var instr = track.info.variable;
                    var v = track.volume / 7;
                    player.queueWaveTable(audioContext, input, window[instr], when, track.notes[i].pitch, duration, v, track.notes[i].slides);
                }
            }
        }
        for (var b = 0; b < song.beats.length; b++) {
            var beat = song.beats[b];
            for (var i = 0; i < beat.notes.length; i++) {
                if (beat.notes[i].when >= start && beat.notes[i].when < end) {
                    var when = songStart + beat.notes[i].when;
                    var duration = 1.5;
                    var instr = beat.info.variable;
                    var v = beat.volume / 2;
                    player.queueWaveTable(audioContext, input, window[instr], when, beat.n, duration, v);
                }
            }
        }
    }
    this.startLoad=function(song) {
        this.log('startLoad',song);
        var AudioContextFunc = window.AudioContext || window.webkitAudioContext;
        audioContext = new AudioContextFunc();
        player = new WebAudioFontPlayer();
        reverberator = player.createReverberator(audioContext);
        reverberator.output.connect(audioContext.destination);
        input = reverberator.input;
        for (var i = 0; i < song.tracks.length; i++) {
            var nn = player.loader.findInstrument(song.tracks[i].program);
            var info = player.loader.instrumentInfo(nn);
            song.tracks[i].info = info;
            song.tracks[i].id = nn;
            player.loader.startLoad(audioContext, info.url, info.variable);
        }
        for (var i = 0; i < song.beats.length; i++) {
            var nn = player.loader.findDrum(song.beats[i].n);
            var info = player.loader.drumInfo(nn);
            song.beats[i].info = info;
            song.beats[i].id = nn;
            player.loader.startLoad(audioContext, info.url, info.variable);
        }
        player.loader.waitLoad(function () {
            self.loadSong(song);
        });
    }
    this.getCurrentSong=function(){
        return loadedsong;
    }
    this.getPosition=function(){
        return currentSongTime;
    }
    this.setPosition=function(position){
        if (loadedsong || stoppedsong) {
            player.cancelQueue(audioContext);
            var next = position; //song.duration * position / 100;
            songStart = songStart - (next - currentSongTime);
            currentSongTime = next;
            lastPosition = currentSongTime;
        }
    }
    this.setVolume=function(volume,track){
        if (loadedsong && loadedsong.tracks[track]){
            player.cancelQueue(audioContext);
            var v = volume / 100;
            if (v < 0.000001) {
                v = 0.000001;
            }
            loadedsong.tracks[track].volume = v;
        } else {
            this.log("setVolume: Track not found",track);
        }
    }
    this.setInstrument=function(value, track){
        if (loadedsong && loadedsong.tracks[track]){
            var nn = value;
            var info = player.loader.instrumentInfo(nn);
            var self = this
            player.loader.startLoad(audioContext, info.url, info.variable);
            player.loader.waitLoad(function () {
                self.log("instrument loaded", info);
                loadedsong.tracks[track].info = info;
                loadedsong.tracks[track].id = nn;
            });
        } else {
            this.log("setInstrument: Track not found",track);
        }
    }
    this.loadSong=function(song) {
        this.stop();
        audioContext.resume();

        this.log("Tracks",song.tracks);
        this.log("Beats",song.beats);
        this.log("Duration", song.duration);
        // this.log("Instruments", player.loader.instrumentKeys());
        // this.log("Drums", player.loader.drumKeys());
        loadedsong = song;
        this.log("Song loaded",loadedsong);
        if (this.onload){
            this.onload(song)
        }
    }
    this.openFile=function(fileObj){
        var midiFile = new MIDIFile(fileObj);
        var song = midiFile.parseSong();
        self.startLoad(song);
    }
    this.handleFileSelect=function(event) {
        var self=this;
        this.log('fileSelect',event);
        var file = event.target.files[0];
        this.log('file',file);
        this.handleBlob(file);
    }
    this.handleBlob = function(blob) {
        var self=this;
        var fileReader = new FileReader();
        fileReader.onload = function (event ) {
            self.log('loaded',event);
            var fileObj = event.target.result;
            self.openFile(fileObj);
        };
        fileReader.readAsArrayBuffer(blob);
    }
    this.handleURL=function(path) {
        this.log('load URL',path);
        var xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.open("GET", path, true);
        xmlHttpRequest.responseType = "arraybuffer";
        xmlHttpRequest.onload = function (e) {
            var arrayBuffer = xmlHttpRequest.response;
            var midiFile = new MIDIFile(arrayBuffer);
            var song = midiFile.parseSong();
            self.startLoad(song);
        };
        xmlHttpRequest.send(null);
    }
    

    if (source){
        if (typeof(source)=="string" && document.getElementById(source)){
            source=document.getElementById(source)
        }
        if (source instanceof HTMLInputElement){
            source.addEventListener('change', this.handleFileSelect.bind(this), false);
        }  else if (source instanceof Blob){
            source.addEventListener('change', this.handleBlob.bind(this), false);
        } else {
            // fallback to read from URL 
            // Note: cross domain requests must pass CORS policy
            this.handleURL(source);
        }  
    }	

}