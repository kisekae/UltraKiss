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
var playerstopped = true ;   // true if MIDI player is stopped
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
const reconnectInterval = 5000; // 5 seconds


// Override console.log to capture output in a file.

const originalLog = console.log;
let logs = [];

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


// Establish websocket connection.

console.log("Host=" + host + " Port=" + port + " SSL=" + ssl);  
initwebsocket(host,port,ssl) ;

function initwebsocket(host,port,ssl)
{
    if (host === null) host = "127.0.0.1" ;
    if (port === null) port = "49152" ;
    var url = "ws://"+host+":"+port+"/ultrakiss" ;
    if (ssl === "true") { 
        url = "wss://"+host+":"+port+"/ultrakiss" ; 
    }
    console.log("WebSocket URL is " + url);   
    ws = new WebSocket(url) ;
    ws.binaryType = "arraybuffer";
}


// Detect browser closing.  Perform an orderly shutdown on the server.

window.addEventListener("beforeunload", function (e) {
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
    let offset = 0;

    const fileReader = new FileReader();

    fileReader.onload = function(event) {        // Send the chunk as binary data
        if (websocket.readyState === WebSocket.OPEN) {
            websocket.send(event.target.result);
        }
        offset += event.target.result.byteLength;
        // Check if more chunks need to be sent
        if (offset < file.size) {
            readNextChunk();
        } else {
            var name = file.name.replaceAll(" ","_") ;            
            console.log("File transfer to server complete, " + name + " size=" + offset);
            if (websocket.readyState === WebSocket.OPEN) {
                websocket.send("fileuploadend " + name + " " + offset + " " + fileopensource + " " + fileopenimport);
            }
        }
    };

    fileReader.onerror = function(error) {
        console.error("Error reading file: " + file.name, error);
    };

    function readNextChunk() {
        const slice = file.slice(offset, offset + CHUNK_SIZE);
        fileReader.readAsArrayBuffer(slice);
    }

    // Start the process.
    // First, send metadata (like filename, size) as a text message
    
    if (websocket.readyState === WebSocket.OPEN) {
        var name = file.name.replaceAll(" ","_") ;            
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

ws.onopen = function() {
//  alert("Opened");
    if (ws.readyState === WebSocket.OPEN) {
        ws.send("screen " + canvas.width + " " + canvas.height);
    }
};

ws.onclose = function(event) {
    console.log("Connection closed with code: " + event.code);
    reason = event.reason ;
    if (reason === null || reason.length === 0) reason = "Unknown" ;
    console.log("Reason for closure: " + reason);
    if (reason.startsWith("UltraKiss")) return ;
//    console.log("Reconnecting...");
//    setTimeout(initwebsocket,reconnectInterval,host,port,ssl) ;
};

ws.onerror = function(err) {
    console.log("Websocket error.");
    if (!(player === undefined)) { player.stop() ; }
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
        // "audiostop id filename filesize" ;
        
        else if (tokens[0] === "audiostop")
        {
            var source = audioSourceMap.get(tokens[1]) ;
            console.log("Stop audio playing for " + tokens[2]);
            if (source === undefined)
               console.log("Undefined source node for " + tokens[2]);
            else   
            {
               source.stop() ;
               playerstopped = true ;
               audioSourceMap.delete(tokens[1]);
               ws.send("audiostop " + tokens[1] + " " + audioNameMap.get(source)) ;
            }
            if (tokens[2] === "all" || source instanceof MIDIPlayer)
            {
               if (!(player === undefined)) 
               { 
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
        
        // Send an inactivity timeout warning.
        
        else if (tokens[0] === "timeoutalert")
        {
            showNonBlockingAlert("Inactivity timeout. Session will close in 30 seconds.");
        }
        
        else if (tokens[0] === "timeoutcancel")
        {
            hideNonBlockingAlert() ;                
        }
        
        // Send an inactivity timeout warning.
        
        else if (tokens[0] === "timeout")
        {
            showNonBlockingAlert("Inactivity timeout. Session has closed.");
        }
        
        // Send an inactivity timeout warning.
        
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
                
                if (w === cw)     
                    ctx.drawImage(img, 0, 0); 
                else
                {
                    ctx.fillStyle = 'black'; // Set fill color
                    ctx.fillRect(0, 0, cw, ch); // Draw a filled rectangle            
                    ctx.drawImage(img, (cw-w)/2, (ch-h)/2); 
                    console.log("Drawing centered image");
               }
            };    
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
    }
};


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

const canvas = document.getElementById('myCanvas');
const ctx = canvas.getContext('2d');

function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    // Redraw content here if needed after resizing
    // Example: ctx.fillRect(0, 0, canvas.width, canvas.height);
    if (ws.readyState === WebSocket.OPEN) {
        ws.send("screen " + canvas.width + " " + canvas.height);
    }
}

// Initial resize
resizeCanvas();

// Resize when the window is resized
window.addEventListener('resize', resizeCanvas);  


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