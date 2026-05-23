// Title:        Kisekae UltraKiss
// Version:      5.0  (December 16, 2025)
// Copyright:    Copyright (c) 2002-2025
// Author:       William Miles
// Description:  Kisekae Set System
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

/*
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  This copyright notice and this permission notice shall be included in      %
%  all copies or substantial portions of UltraKiss.                           %
%                                                                             %
%  The software is provided "as is", without warranty of any kind, express or %
%  implied, including but not limited to the warranties of merchantability,   %
%  fitness for a particular purpose and noninfringement.  In no event shall   %
%  William Miles be liable for any claim, damages or other liability,         %
%  whether in an action of contract, tort or otherwise, arising from, out of  %
%  or in connection with Kisekae UltraKiss or the use of UltraKiss.           %
%                                                                             %
%  William Miles                                                              %
%  144 Oakmount Rd. S.W.                                                      %
%  Calgary, Alberta                                                           %
%  Canada  T2V 4X4                                                            %
%                                                                             %
%  w.miles@wmiles.com                                                         %
%                                                                             %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
*/


var ws ;

var captureInProgress = false ;
var fileName = "" ;          // name of file downloaded from server
var fileopensource = "" ;    // Unique id for java object initiating file open
var fileopenimport = "" ;    // if set then open for edit import
var fileopenmultiple = "" ;  // if set then allow multiple file selection
var fileopentype = "" ;      // if set then list of acceptable file types
var filesavesource = "" ;    // Unique id for java object initiating file save
var audiosource = "" ;       // Unique id for java object initiating audio sound
var fileChunks = [] ;        // buffer for file transfer of binary data chunk
var audioChunks = [] ;       // buffer for audio transfer of binary data chunk
var expectedFileSize = 0 ;   // expected size of file downloaded from server
var receivedFileSize = 0 ;   // received size of downloaded file message chunk
var expectedAudioSize = 0 ;  // expected size of audio downloaded from server
var receivedAudioSize = 0 ;  // received size of downloaded file message chunk
var retransmitCount = 0 ;    // number of retransmissions attempted
var screenrate = 0 ;         // number of screen transmissions received
var timeoutwarnings = 0 ;    // number of timeout warnings sent
var inactivityalert = null;  // inactivity timer function
var first = true ;           // set false after first screen capture
var cancelupload = false ;   // true if file upload to server stopped
var playerstopped = true ;   // true if MIDI player is stopped
var terminated = false ;     // true if UltraKiss terminated on server
var player ;                 // MIDI player

// Translation maps to convert a java object unique id to an audio source node 
// and an audio source node to a file name.

const audioSourceMap = new Map() ; // map audiosource id to source node
const audioNameMap = new Map() ;   // map source node to file name

var audioContext ;           // global 
var host = document.currentScript.getAttribute('host'); // 
var port = document.currentScript.getAttribute('port'); // 
var ssl = document.currentScript.getAttribute('ssl'); // 

let consolelog = 'client' + port + '.log' ; // console log file name
const reconnectInterval = 1000; // 1 seconds
var reconnectcount = 0 ;        // reconnect attempts
const maxreconnect = 1 ;        // maximum reconnects

const canvas = document.getElementById('myCanvas');
const ctx = canvas.getContext('2d');
canvas.width = window.innerWidth;
canvas.height = window.innerHeight;


// Override console.log to capture output in a file.

const originalLog = console.log;
let logs = [];
console.log("UltraKiss websocket client.js released May 23, 2026") ;  

console.log = function() {
    // Log to the console (optional, if you still want to see output there)
    // Store arguments in a readable string format
    let message = Array.from(arguments).join(' ');
    logs.push(message); 
    originalLog.apply(console, arguments);
};

// Function to download the logs as a file to the client computer.
// Not used.  Rather, we download the console log to the server when 
// UltraKiss terminates.  See "sendconsolelog" websocket message and
// uploadLogFile() function.

window.downloadLogs = function () {
   let logContent = logs.join('\n');
   let blob = new Blob([logContent], { type: 'text/plain' });
   let url = URL.createObjectURL(blob);

   // Create a link element to trigger the download
   let a = document.createElement('a');
   a.href = url;
   a.download = consolelog ;
   document.body.appendChild(a);
   a.click();

   // Clean up
   document.body.removeChild(a);
   URL.revokeObjectURL(url);
};

// Timestamp our console log file.
const now = new Date() ;
console.log(now) ;


// Detect browser closing.  Perform an orderly shutdown on the server.

window.addEventListener("beforeunload", function (e) {
  if (!terminated)
  {
     e.preventDefault();
     // Chrome requires returnValue to be set.
     e.returnValue = '';
  }
});

window.addEventListener('unload', () => {
  // Perform minor synchronous cleanup tasks here
  console.log('Browser tab is closing...');
  shutdown() ;
});

async function shutdown() {
  console.log('Window closing...');
  ws.send("close window") ;
  // Wait for 2 seconds (2000 milliseconds)
  await sleep(2000);
  console.log('Window closed.');
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}


/*
 * Define our file open dialog.  Select a file on the client and
 * upload this file to the server.  We use a hidden input element
 * for client file selection and capture the "change" when a 
 * selection is made to identify the file name.
 * <input type="file" id="fileInput" style="display: none;" />
 */

const fileInput = document.getElementById("fileInput") ;

// Add an event listener to the file input to handle file selection

fileInput.addEventListener('change', function(event) {
  // Get the selected file(s) from the event target
  const files = event.target.files;

  if (files && files.length > 0) {
//    audioContext = new (window.AudioContext || window.webkitAudioContext)(); // 
    // Consider setting the sample rate to match the server's audio to avoid issues
    // const audioContext = new AudioContext({ sampleRate: 16000 });
    const file = files[0]; // Process the first file
    uploadFile(file,ws) ;
  }
});


// Send a client file to the server.  The "fileopen" message from the
// UltraKiss application on the server causes a change() event on the
// fileInput element in the HTML document.  This initiates the reading 
// of the file and the sending of chunks of binary data to the server.

function uploadFile(file, websocket) {
    const CHUNK_SIZE = 1024 * 32 ; // 32 KB chunks
    const MAX_BUFFER = 1024 * 1024; // 1MB capacity before pausing
    let lastSentThreshold = 0;
    var percentComplete = 0 ;
    let offset = 0;
    var first = true ;             // true for first throttle message

    const fileReader = new FileReader();

    fileReader.onload = function(event) {        // Send the chunk as binary data
        var name = file.name.replaceAll(" ","_") ;            
        offset += event.target.result.byteLength;
        if (websocket.readyState === WebSocket.OPEN) {
            websocket.send(event.target.result);
            percentComplete = ((offset * 100) / file.size).toFixed(0) ;
            if (percentComplete >= lastSentThreshold + 10) {
                lastSentThreshold = Math.floor(percentComplete / 10) * 10; 
                websocket.send("fileuploadprogress " + name + " " + percentComplete);
                console.log("fileuploadprogress " + name + " " + percentComplete);
            }
        }
        // Check if more chunks need to be sent
        if (offset < file.size) {
            readNextChunk();
        } else {
            console.log("File upload to server complete, " + name + " size = " + offset);
            if (websocket.readyState === WebSocket.OPEN) {
                websocket.send("fileuploadend " + name + " " + offset + " " + fileopensource + " " + fileopenimport);
            }
        }
    };

    fileReader.onerror = function(error) {
        console.error("Error reading file: " + file.name, error);
    };

    function readNextChunk() {
        var name = file.name.replaceAll(" ","_") ;            
        if (websocket.readyState === WebSocket.OPEN && !cancelupload) {
            // Limit bufferedAmount to prevent memory issues
            if (websocket.bufferedAmount > MAX_BUFFER) { // 1MB threshold
                if (first) {
                    console.log("fileupload " + name + " waiting, buffer full, at " + offset + " (" + percentComplete + "%)");      
                    first = false ;
                }
                setTimeout(readNextChunk, 10);
                return;
            }
            const slice = file.slice(offset, offset + CHUNK_SIZE);
            fileReader.readAsArrayBuffer(slice);
            first = true ;
        } else {
            console.log("fileupload " + name + " cancelled.");     
        }
    }
    
    // Start the process.
    // First, send metadata (like filename, size) as a text message
    
    if (websocket.readyState === WebSocket.OPEN) {
        var name = file.name.replaceAll(" ","_") ;            
        console.log("fileupload " + name + " begins, size = " + file.size);           
        cancelupload = false ;
        websocket.send("fileupload " + name + " " + file.size + " " + fileopenimport);
    }
    readNextChunk();
}


// Send the client log file to the server.  The "sendconsolelog" message 
// from the UltraKiss application on the server invokes this function.

function uploadLogFile(filename, websocket) {
    var name = filename ;
    const CHUNK_SIZE = 1024 * 32 ; // 32 KB chunks
   
    // Start the process.
    // First, send metadata (like filename, size) as a text message.
    
    let logContent = logs.join('\n');
    let blob = new Blob([logContent], { type: 'text/plain' });
       
    if (websocket.readyState === WebSocket.OPEN) {
        name = name.replaceAll(" ","_") ;            
        websocket.send("fileupload " + name + " " + blob.size);
    }
    
    // Send the blob as binary data.
    if (websocket.readyState === WebSocket.OPEN) {
        sendBlobInChunks(websocket,blob,CHUNK_SIZE) ;
    }
    
    console.log("Log file transfer to server complete, " + name + " size=" + blob.size);
    if (websocket.readyState === WebSocket.OPEN) {
        websocket.send("fileuploadlogfileend " + name + " " + blob.size);
    }
}

function sendBlobInChunks(ws, blob, chunkSize) {
    const totalSize = blob.size;
    let offset = 0;

    function sendNextChunk() {
        if (offset < totalSize) {
            const chunk = blob.slice(offset, offset + chunkSize);
            ws.send(chunk);
            offset += chunkSize;

            // Optional: Add a slight delay to prevent network flooding, 
            // or wait for an acknowledgment from the server before sending the next chunk.
            // setTimeout(sendNextChunk, 10); 
            sendNextChunk(); // Send immediately if not worried about network saturation
        }
    }

    sendNextChunk() ;
}


/* 
 * WebSocket events.  
 */


// Establish websocket connection.

function initwebsocket(host,port,ssl)
{
    console.log("Host=" + host + " Port=" + port + " SSL=" + ssl);  
    if (host === null) host = "127.0.0.1" ;
    if (port === null) port = "49152" ;
    var url = "ws://"+host+":"+port+"/ultrakiss" ;
    if (ssl === "true") { 
        url = "wss://"+host+":"+port+"/ultrakiss" ; 
    }
    console.log("Connection WebSocket URL is " + url);   
    ws = new WebSocket(url) ;
    ws.binaryType = "arraybuffer";
}


function connectWebSocket() {
    initwebsocket(host,port,ssl) ;

ws.onopen = function(event) {
    console.log('Connection established');
    // Clear any previous reconnection timer on successful connection
    if (window.reconnectTimeoutId) {
        clearTimeout(window.reconnectTimeoutId);
        window.reconnectTimeoutId = null;
    }
    if (ws.readyState === WebSocket.OPEN) {
        console.log("screen " + canvas.width + " " + canvas.height);
        ws.send("screen " + canvas.width + " " + canvas.height);
    }
    // Start the loop to report the screen rate to the server.
//    measureScreenRate(); 
};


ws.onclose = function(event) {
    console.log("Connection closed with code: " + event.code);
    reason = event.reason ;
    if (reason === null || reason.length === 0) reason = "Unknown" ;
    console.log("Reason for closure: " + reason);
    if (reason.startsWith("UltraKiss")) return ;
    if (terminated) return ;
    
    if (reconnectcount <= maxreconnect)
    {
        console.log("Reconnecting...");
        // Implement a delay before attempting to reconnect to prevent rapid, continuous attempts
        if (!window.reconnectTimeoutId) { // Prevent multiple timers from firing
            window.reconnectTimeoutId = setTimeout(function() {
                reconnectcount++ ;
                connectWebSocket();
            }, 5000); // Reconnect after 5 seconds (5000 milliseconds)
        }
    }
    else
        console.log("Maximum reconnection attempts exceeded.") ;
};

ws.onerror = function(err) {
    console.log("Websocket error. host="+host+" port="+port+" ssl="+ssl);
    if (!(player === undefined)) { player.stop() ; }
    ws.close() ;
//  alert("Server is not available.");
};

// This is the general WebSocket message receipt function for the client.  
// It receives text or binary data.  Text messages are control messages and 
// binary messages are transfer of file or sound data.  On receipt of a message 
// from the server we either draw a screen image or capture file or sound.

ws.onmessage = function (evt) { 
    
    // Text input.
    
    if (typeof event.data === "string") {
       let message = event.data ;
       if (!message.includes("cursor"))
          console.log("message: " + event.data);
       const tokens = event.data.split(" ") ;
        
        // Cursor change 
        // "cursor cursortype"
        
        if (tokens[0] === "cursor")
           setCursor(tokens[1]) ;
       
        // Show file open dialog to begin uploading a client file to the server.
        // This is the click() event that initiates the uploadFile() process.
        // "fileopen id classname import multiple extension-list"
        
        else if (tokens[0] === "fileopen")
        {
           const fileInput = document.getElementById('fileInput');
           fileopenimport = " ";
           fileopenmultiple = " ";
           fileopentype = " ";
           fileopensource = tokens[1] ;
           if (tokens.length > 3) fileopenimport = tokens[3] ;
           if (tokens.length > 4) 
           {
               fileopenmultiple = tokens[4] ;
               if (fileopenmultiple === "true")
               {
                   fileInput.setAttribute('multiple', 'true'); 
                   console.log("fileopen multiple: " + fileopenmultiple);
               }
           }
           if (tokens.length > 5) 
           {
               fileopentypes = tokens.slice(5) ;
               fileopentype = fileopentypes.join(" ") ;
               fileInput.setAttribute('accept', fileopentype); 
               console.log("fileopen accept: " + fileopentype);
           }
           fileInput.click() ;      
        }
        
        else if (tokens[0] === "cancelupload")
        {
            cancelupload = true ;
        }
        
        // This begins the process for downloading a server file to the client.
        // Any binary file type messages that follow are the file data.  The 
        // transfer terminates on receipt of a "filesaveend" text message sent
        // by the server after it has sent all the file data.
        // "filesave id filename filesize"
        
        else if (tokens[0] === "filesave")
        {
           filesavesource = tokens[1] ;
           fileName = tokens[2] ;
           expectedFileSize = tokens[3] ;
           fileChunks = [] ;
           receivedFileSize = 0;
           console.log("Starting download of " + fileName + " to client, size=" + expectedFileSize + " bytes");
        }
        
        // This is the signal from the server that all the file data has been sent.
        // Put the binary file data into an HTML anchor URL href so that it can
        // be stored as a file when the <a href=...> is clicked.
        // "filesaveend filename filesize"
        
        else if (tokens[0] === "filesaveend")
        {
            // Reassemble the file when finished
            const blob = new Blob(fileChunks, { type: 'application/octet-stream' });
            const url = URL.createObjectURL(blob);

            // Create a temporary link and create the file on the client.
            const a = document.createElement('a');
            a.href = url;
            a.download = fileName || 'downloaded_file';
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url); // Clean up
            console.log("File transfer to client complete, " + fileName + " size=" + blob.size);
        }
        
        // This begins the process for downloading an audio file from the server 
        // to the client.  The transfer terminates on receipt of a "audioend" or
        // "audioendmidi" text message sent by the server after it has sent all 
        // the sound data.  An "audioendmidi" message is sent for MIDI files and
        // an "audioend" message is sent for all other sound files.
        // "audioopen id filename filesize"
       
        else if (tokens[0] === "audioopen")
        {
           audiosource = tokens[1] ;
           fileName = tokens[2] ;
           expectedAudioSize = tokens[3] ;
           audioChunks = [] ;
           receivedAudioSize = 0;
           console.log("Starting download of " + fileName + " to client, size=" + expectedAudioSize + " bytes");
        }
        
        // End receipt of sound file from server.  Play the sound.    When the 
        // sound terminates send an "audiostop" message back to the server.  
        // This lets the server perform any end-of-sound actions required.
        // "audioend filename filesize"
        
        else if (tokens[0] === "audioend")
        {    
            if (receivedAudioSize != expectedAudioSize)
            {
                console.log("Audio transfer failure, received=" + receivedAudioSize + ", expected=" + expectedAudioSize + ", filename=" + fileName);
                retransmitCount++ ;
                if (retransmitCount < 2)
                {
                    console.log("Attempting retransmit, source="+audiosource+" filename="+fileName);
                    ws.send("retransmit "+audiosource+" "+fileName+" "+expectedAudioSize+" "+receivedAudioSize) ;
                    return ;
                }
                console.log("Continuing anyway, retransmit has been attempted ...");
                ws.send("notify retransmit failed for "+audiosource+" "+fileName+" "+expectedAudioSize+" "+receivedAudioSize+" continuing anyway ...") ;
            }
            
            // Reassemble the file when finished.
            retransmitCount = 0 ;
            const blob = new Blob(audioChunks, { type: 'application/octet-stream' });
            blob.arrayBuffer().then((result) => {
//              audioContext = new (window.AudioContext || window.webkitAudioContext)(); 
                var AudioContext = window.AudioContext || window.webkitAudioContext;
                var audioContext = new AudioContext({
                  latencyHint: 'interactive',
                  sampleRate: 48000
                });
                audioContext.decodeAudioData(result, function(buffer) {
                    const source = audioContext.createBufferSource() ;
                    // Map the audio source node to the java object and file name.
                    audioSourceMap.set(audiosource,source) ;
                    audioNameMap.set(source,tokens[1]) ;
                    
                    source.buffer = buffer;
                    source.connect(audioContext.destination);
                    console.log("Playback begins for "+audioNameMap.get(source));
                    ws.send("notify playback begins for "+audioNameMap.get(source)) ;
                    source.start(0); // Play immediately
                    
                    source.onended = () => {
                       console.log("Playback finished for "+audioNameMap.get(source));
                       ws.send("notify playback finished for "+audioNameMap.get(source)) ;
                       // Send an audiostop message for this sound back to the server.
                       for (const [key, value] of audioSourceMap) 
                       {
                          if (value === source) 
                          {
                             ws.send("audiostop " + key + " " + audioNameMap.get(source)) ;
                             audioSourceMap.delete(key) ;
                             audioNameMap.delete(source) ;
                          }
                       }                           
                    };
                }, function(e) { 
                    console.error("Error decoding audio data: " + e); 
                    ws.send("audiostop " + audiosource + " " + tokens[1]) ;
                });
            });
            console.log("Audio transfer to client complete, " + fileName + " size=" + blob.size);
        }
        
        // End receipt of midi file from server.  Play the sound.  When the 
        // sound terminates send an "audiostop" message back to the server.
        // "audioendmidi filename filesize"
        
        else if (tokens[0] === "audioendmidi")
        {    
            // Reassemble the file when finished
            const blob = new Blob(audioChunks, { type: 'application/octet-stream' });
            const url = URL.createObjectURL(blob);

            // Only one midi at a time
            if (!(player === undefined) && !playerstopped) 
            { 
               console.log("midi player stoppped: " + audioNameMap.get(player));
               player.stop() ;
               playerstopped = true ;
               ws.send("audiostop " + tokens[1] + " " + audioNameMap.get(player)) ;
               window.onblur = null ;
               window.onfocus = null ;
            }

            // https://github.com/fraigo/javascript-midi-player
            player = new MIDIPlayer(url) ;
            console.log("new player assigned") ;
            
            
            // the load event is triggered when the player is loaded
            player.onload = function(song){
                console.log("Playback begins for "+audioNameMap.get(player));
                ws.send("notify playback begins for "+audioNameMap.get(player)) ;
                player.play() ;
                playerstopped = false ;
            } ;
            
            // The end event is triggered when the song ends
            player.onend=function(){
                player.stop() ;  // necessary to terminate the player.
                console.log("Playback finished for "+audioNameMap.get(player));
                ws.send("notify playback finished for "+audioNameMap.get(player)) ;
                playerstopped = true ;
                console.log("Removing windows events for " + tokens[1]);
                window.onblur = null ;
                window.onfocus = null ;
                
                // Send an audiostop message for this sound back to the server.
                for (const [key, value] of audioSourceMap) 
                {
                    if (value === player) 
                    {
                        ws.send("audiostop " + key + " " + audioNameMap.get(player)) ;
                        audioSourceMap.delete(key) ;
                        audioNameMap.delete(source) ;
                    }
                }  
            } ;
            
            // stop playing when the window is unfocused
	    window.onblur=function(){
		console.log("Blur", new Date()) ;
		if (!(player === undefined)) player.pause();
	    } ;
            
	    // resume playing when window is in focus
	    window.onfocus=function(){
		console.log("Focus", new Date()) ;
		if (!(player === undefined) && !playerstopped) 
                    player.play();
	    } ;      
            
            // Map the audio source node to the java object and file name.
            if (!(player === undefined))
            {
                audioSourceMap.set(audiosource,player) ;
                audioNameMap.set(player,tokens[1]) ;
            }
        }
        
        // An "audiostop" message received by the client stops the named sound 
        // from being played.  The client can be playing more than one sound
        // concurrently.  We use the source map to find the audio playback 
        // source based on the file name.
        // "audiostop id filename filesize type" ;
        
        else if (tokens[0] === "audiostop")
        {
            var source = audioSourceMap.get(tokens[1]) ;
            console.log("Stop audio playing for " + tokens[2]);
            if (source === undefined)
               console.log("Undefined source node for " + tokens[2]);
            else   
            {
               console.log("source stoppped: " + audioNameMap.get(source));
               source.stop() ;
               playerstopped = true ;
               audioSourceMap.delete(tokens[1]);
               ws.send("audiostop " + tokens[1] + " " + audioNameMap.get(source)) ;
            }
            if (tokens[2] === "all" || source instanceof MIDIPlayer)
            {
               if (!(player === undefined)) 
               { 
                   console.log("midi player all stoppped: " + audioNameMap.get(player));
                   player.stop() ;
                   playerstopped = true ;
                   ws.send("audiostop " + tokens[1] + " " + audioNameMap.get(player)) ;
               }
               console.log("Removing windows events for " + tokens[2]);
               window.onblur = null ;
               window.onfocus = null ;
            }
        }
        
        // Send the console log file
        
        else if (tokens[0] === "sendconsolelog")
        {
            uploadLogFile(consolelog,ws) ;
        }
        
        // Show an inactivity timeout warning.
        
        else if (tokens[0] === "timeoutalert")
        {
            timeoutwarnings = 0;
            inactivityalert = setInterval(sendTimeoutWarning, 1000);
        }
        
        else if (tokens[0] === "timeoutcancel")
        {
            clearInterval(inactivityalert);
            inactivityalert = null;
            hideNonBlockingAlert() ;                
        }
                
        else if (tokens[0] === "timeout")
        {
            clearInterval(inactivityalert);
            inactivityalert = null;
            showNonBlockingAlert("Inactivity timeout. Session has closed.");
        }
        
        // Show a non-blocking message.
        
        else if (tokens[0] === "notify")
        {
            const msgtokens = tokens.slice(1) ;
            const textmsg = msgtokens.join(" ") ;
            showNonBlockingAlert(textmsg) ;
        }
        
        else if (tokens[0] === "notifycancel")
        {
            hideNonBlockingAlert() ;                
        }
        
        // Show a blocking message.
        
        else if (tokens[0] === "alert")
        {
            const msgtokens = tokens.slice(1) ;
            const textmsg = msgtokens.join(" ") ;
            alert(textmsg) ;
        }
        
        // A "browser" message attempts to open a new browser window.
        // Run this in a setTimeout() function to bypass pop-up blockers.
        // "browser url"
        
        else if (tokens[0] === "browser")
        {
            setTimeout(() => { window.open(tokens[1], '_blank'); }, 100);    
        }
                
        // Close the session websocket.
        
        else if (tokens[0] === "close")
        {
            console.log("Close socket request on client.");
            window.onblur = null ;
            window.onfocus = null ;
            terminated = true ;
            ws.close(1000,"UltraKiss terminated.") ;
        }
    } 
    
    // Binary input (image or file or sound) is identifed by a one byte header
    // that prefixes the binary data.  This header is used to determine how
    // the binary data is to be processed.  Note that file and sound data is
    // typically sent by the server in chunks that are assembled into a complete
    // message by the websocket before being delivered to this function.
    
    else if (event.data instanceof ArrayBuffer) {
        const headerSize = 1;
        var message = new Uint8Array(evt.data);
        var messageType = message[0] ;
        var actualData = message.slice(headerSize) ;
        
        // Message type 0 is used to draw screen image on canvas.  The image 
        // is centered in the canvas if it is not of the canvas width.  The
        // server will send screen captures on an unsolicited basis.  This
        // screen refresh is typically performed many times a second.
        
        if (messageType === 0) {
            var data = "";
            var bytes = actualData;
            var len = bytes.byteLength;
            for (var i = 0; i < len; ++i) { data += String.fromCharCode(bytes[i]); }
            const canvas = document.getElementById('myCanvas'); // Replace 'myCanvas' with your canvas ID
            const ctx = canvas.getContext('2d');
            const img = new Image();
            img.onload = function() 
            { 
                const w = img.width ;
                const h = img.height ;
                const cw = canvas.width ;
                const ch = canvas.height ;
                
                if (first) {
                    console.log("first screen capture (" + w + "," + h + ")");
                    first = false ;
                }
                if (w === cw && h === ch)     
                    ctx.drawImage(img, 0, 0); 
                else
                {
                    ctx.fillStyle = 'black'; // Set fill color
                    ctx.fillRect(0, 0, cw, ch); // Draw a filled rectangle            
                    ctx.drawImage(img, (cw-w)/2, (ch-h)/2); 
                    console.log("Drawing centered image");
               }
            };    
            screenrate++ ;
            img.src = "data:image/png;base64,"+window.btoa(data);
        }
        
        // Message type 1 represents file data.  The "filesave" message from the
        // UltraKiss application sets up the environment so that any following
        // binary file type data messages sent by the server are saved as chunks 
        // until a "filesaveend" message is received from the server.  At this  
        // time an <a download=filename href=urldata> element is added to the 
        // HTML document and clicked, which opens a file dialog in the browser 
        // to save the binary data to the file.
        // 
        // Although Jetty is sending small buffers through a sendPartialBinary 
        // it turns out that the buffers are assembled by the websocket and  
        // only one total chunk is passed on to this method.
        
        if (messageType === 1)
        { 
            var bytes = actualData;
            var len = bytes.byteLength;
            fileChunks.push(bytes) ;
            console.log("Received file chunk size=" + bytes.length + " len=" + len);
            receivedFileSize += len ;
            const progress = (receivedFileSize / expectedFileSize) * 100;
            console.log(`File progress: ${progress.toFixed(2)}%`);            
        }
        
        // Message type 2 represents sound data.  This is treated similarly to
        // file data.
        
        if (messageType === 2)
        { 
            var bytes = actualData;
            var len = bytes.byteLength;
            audioChunks.push(bytes) ;
            console.log("Received audio chunk size=" + bytes.length + " len=" + len);
            receivedAudioSize += len ;
            const progress = (receivedAudioSize / expectedAudioSize) * 100;
            console.log(`Audio progress: ${progress.toFixed(2)}%`);   
        }       
        
        // Message type 3 is used to draw a clipped screen image on canvas.  
        // The next 16 bytes represent four 4-byte integers (x,y,width,height)
        // that describe the dirty area of the screen that needs to be drawn.  
        // The server will send screen captures on an unsolicited basis.  This
        // screen refresh is typically performed many times a second.
        
        if (messageType === 3) {
            var data = "";
            var bytes = actualData;
            var len = bytes.byteLength;
            if (len <= 16) {
                console.log("clipped screen capture buffer too small, " + len);
                return ;                
            }
            const x = (bytes[0] << 24) | (bytes[1] << 16) | (bytes[2] << 8) | bytes[3];
            const y = (bytes[4] << 24) | (bytes[5] << 16) | (bytes[6] << 8) | bytes[7];
            const w = (bytes[8] << 24) | (bytes[9] << 16) | (bytes[10] << 8) | bytes[11];
            const h = (bytes[12] << 24) | (bytes[13] << 16) | (bytes[14] << 8) | bytes[15];
            for (var i = 16; i < len; ++i) { data += String.fromCharCode(bytes[i]); }
            const canvas = document.getElementById('myCanvas'); // Replace 'myCanvas' with your canvas ID
            const ctx = canvas.getContext('2d');
            const img = new Image();
            img.onload = function() 
            { 
                const iw = img.width ;
                const ih = img.height ;
                const cw = canvas.width ;
                const ch = canvas.height ;
                
                if (first) {
                    console.log("first screen capture ("+iw+","+ih+") drawn at ("+x+","+y+","+w+","+h+")");
                    first = false ;
                }
                
                ctx.drawImage(img, x, y, w, h); 
            };    
            screenrate++ ;
            img.src = "data:image/png;base64,"+window.btoa(data);
        }
    }
};
}


// Send the received screen capture rate per second to the server.

function measureScreenRate() {
    if (ws.readyState === WebSocket.OPEN) {
        const period = 5 ;
        const screenratepersec = screenrate / period ;
        ws.send("refreshrate " + screenratepersec);
        screenrate = 0 ;
        // Schedule the next execution after this one finishes
        setTimeout(measureScreenRate, period * 1000);
    }
    console.log('Screen rate transmission terminated.');
}


function sendTimeoutWarning() {
    var n = 30 - timeoutwarnings ;
    showNonBlockingAlert("Inactivity timeout. Session will close in " + n + " seconds.");
    timeoutwarnings++ ;
    beep();
}


function beep() {
    var snd = new Audio("data:audio/wav;base64,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qJrx4KOQjahZxWbcZgztj2c49nKmkId44S71j0c8eV9yDK6uPRzx5X18eDvjvQ6yKo9ZSS6l//8elePK/Lf//IInrOF/FvDoADYAGBMGb7FtErm5MXMlmPAJQVgWta7Zx2go+8xJ0UiCb8LHHdftWyLJE0QIAIsI+UbXu67dZMjmgDGCGl1H+vpF4NSDckSIkk7Vd+sxEhBQMRU8j/12UIRhzSaUdQ+rQU5kGeFxm+hb1oh6pWWmv3uvmReDl0UnvtapVaIzo1jZbf/pD6ElLqSX+rUmOQNpJFa/r+sa4e/pBlAABoAAAAA3CUgShLdGIxsY7AUABPRrgCABdDuQ5GC7DqPQCgbbJUAoRSUj+NIEig0YfyWUho1VBBBA//uQZB4ABZx5zfMakeAAAAmwAAAAF5F3P0w9GtAAACfAAAAAwLhMDmAYWMgVEG1U0FIGCBgXBXAtfMH10000EEEEEECUBYln03TTTdNBDZopopYvrTTdNa325mImNg3TTPV9q3pmY0xoO6bv3r00y+IDGid/9aaaZTGMuj9mpu9Mpio1dXrr5HERTZSmqU36A3CumzN/9Robv/Xx4v9ijkSRSNLQhAWumap82WRSBUqXStV/YcS+XVLnSS+WLDroqArFkMEsAS+eWmrUzrO0oEmE40RlMZ5+ODIkAyKAGUwZ3mVKmcamcJnMW26MRPgUw6j+LkhyHGVGYjSUUKNpuJUQoOIAyDvEyG8S5yfK6dhZc0Tx1KI/gviKL6qvvFs1+bWtaz58uUNnryq6kt5RzOCkPWlVqVX2a/EEBUdU1KrXLf40GoiiFXK///qpoiDXrOgqDR38JB0bw7SoL+ZB9o1RCkQjQ2CBYZKd/+VJxZRRZlqSkKiws0WFxUyCwsKiMy7hUVFhIaCrNQsKkTIsLivwKKigsj8XYlwt/WKi2N4d//uQRCSAAjURNIHpMZBGYiaQPSYyAAABLAAAAAAAACWAAAAApUF/Mg+0aohSIRobBAsMlO//Kk4soosy1JSFRYWaLC4qZBYWFRGZdwqKiwkNBVmoWFSJkWFxX4FFRQWR+LsS4W/rFRb/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////VEFHAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAU291bmRib3kuZGUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMjAwNGh0dHA6Ly93d3cuc291bmRib3kuZGUAAAAAAAAAACU=");  
    snd.currentTime = 0;
    snd.play();
}

// This is an explicit request to capture a screen image from the server.
// Not used.  The server sends unsolicted screen images to the client
// from a thread running in JettyWebSocketEndpoint.

function requestScreen()
{
    if (!captureInProgress) 
    {
        captureInProgress = true ;
        setTimeout(() => { ws.send("capture"); captureInProgress = false ; }, 100);    
    }
}


// Send an audio stop message to the server.  The client does this when audio
// stops playing.

function sendAudioStop(source)
{
   // Send an audiostop message for this sound back to the server.
   for (const [key, value] of audioSourceMap) 
   {
      if (value === source) 
      {
         ws.send("audiostop " + key + " " + audioNameMap.get(source)) ;
         audioSourceMap.delete(key) ;
         audioNameMap.delete(source) ;
      }
   }                           
}


/* 
 * Canvas and Document events.  These are used to capture user actions and
 * send these to the server.
 */

function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    // Redraw content here if needed after resizing
    // Example: ctx.fillRect(0, 0, canvas.width, canvas.height);
    console.log("Resize canvas, width=" + canvas.width + " height=" + canvas.height);  
    if (ws.readyState === WebSocket.OPEN) {
        ws.send("screen " + canvas.width + " " + canvas.height);
    }
}

// Initial resize
// resizeCanvas();

// Resize when the window is resized
window.addEventListener('resize', resizeCanvas);  

// Initial connection attempt
connectWebSocket();


// Mouse events 
canvas.addEventListener('mousemove', (event) => {
    // Get the mouse coordinates relative to the canvas
    const rect = canvas.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;
    if (ws.readyState === WebSocket.OPEN) {
        ws.send("mousemove" +" "+mouseX+" "+mouseY) ;
    }
}) ;
    
canvas.addEventListener('mousedown', function(event) {
    const rect = canvas.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;
    // Send the coordinates as a JSON message (or a simple string)
    if (ws.readyState === WebSocket.OPEN) {
        ws.send("mousedown" +" "+mouseX+" "+mouseY+" "+event.which) ;
    }
});

canvas.addEventListener('mouseup', function(event) {
    const rect = canvas.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;
    // Send the event data to the Java server via WebSocket
    if (ws.readyState === WebSocket.OPEN) {
        ws.send("mouseup" +" "+mouseX+" "+mouseY+" "+event.which) ;
    }
});

canvas.addEventListener('wheel', function(event) {
    // Determine scroll direction and amount
    const deltaY = event.deltaY; // Vertical scroll amount
    const deltaX = event.deltaX; // Horizontal scroll amount (less common)
    const deltaMode = event.deltaMode; // Unit of delta values (pixels, lines, or pages)
    const ctrlKey = event.ctrlKey; // true if CTRL key is pressed
    // Send the event data to the Java server via WebSocket
    if (ws.readyState === WebSocket.OPEN) {
        ws.send("mousewheel" +" "+deltaX+" "+deltaY+" "+deltaMode+" "+ctrlKey) ;
    }
});

document.addEventListener('keydown', function(event) {
    if (ws.readyState === WebSocket.OPEN) {
        event.preventDefault();
        if (event.key === " ")
            ws.send("keypress" +" "+"Space") ;
        else
            ws.send("keypress" +" "+event.key) ;
    }
});

document.addEventListener('keyup', function(event) {
    if (ws.readyState === WebSocket.OPEN) {
        event.preventDefault();
        if (event.key === " ")
            ws.send("keyrelease" +" "+"Space") ;
        else
            ws.send("keyrelease" +" "+event.key) ;
    }
});

document.addEventListener('contextmenu', function(event) {
    event.preventDefault(); // Prevents the default right-click context menu
});

/* 
 * Cursor control - cursorStyle as string
 * 
 * auto        move           no-drop      col-resize
 * all-scroll  pointer        not-allowed  row-resize
 * crosshair   progress       e-resize     ne-resize
 * default     text           n-resize     nw-resize
 * help        vertical-text  s-resize     se-resize
 * inherit     wait           w-resize     sw-resize
 */

function setCursor(cursorStyle) {
  if (canvas.style) canvas.style.cursor=cursorStyle;
}

/* We use a custom dialog message for timeout alerts.  The regular javascript
 * alert() function is modal and blocks.  This requires HTML in the document.
 * 
 * <div><div id="custom-alert" style="display: none; padding: 15px; background-color: #f44336; color: white; position: fixed; top: 10px; right: 10px; z-index: 1000;">
 *  <span id="alert-message"></span>
 * </div></div> 
 */
function showNonBlockingAlert(message) {
    const alertBox = document.getElementById('custom-alert');
    const alertMessage = document.getElementById('alert-message');
    alertMessage.textContent = message;
    alertBox.style.display = 'block';
}

function hideNonBlockingAlert() {
    const alertBox = document.getElementById('custom-alert');
    alertBox.style.display = 'none';
}