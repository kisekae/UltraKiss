<?php
// Title:        Kisekae UltraKiss
// Version:      5.0  (February 23, 2026)
// Copyright:    Copyright (c) 2002-2026
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

echo "UltraKiss Email Post" . PHP_EOL ;
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Collect and sanitize input data
    $name = htmlspecialchars($_POST['name']);
    $email = htmlspecialchars($_POST['email']);
    $title = htmlspecialchars($_POST['title']);
    $description = htmlspecialchars($_POST['description']);
    echo "Title: $title" . PHP_EOL ;

    // Recipient email address
    $to = "w.miles@wmiles.com";
    $subject = "New UltraKiss Issue Submission from $name";
    
    // Generate unique boundary
    $boundary = md5(time());

    // Email headers
    $headers = "From: $email" . "\r\n" ;
    $headers .= "Cc: $email" . "\r\n"; // Adding the CC header
    $headers .= "MIME-Version: 1.0\r\n" ;    
    $headers .= "Content-Type: multipart/mixed; boundary=\"{$boundary}\"\r\n";

    // Email body
    $body = "--{$boundary}\r\n";
    $body .= "Content-Type: text/plain; charset=\"UTF-8\"\r\n\r\n";
    $body .= "Name: $name\r\n\r\n";
    $body .= "Email: $email\r\n\r\n";
    $body .= "Title: $title\r\n\r\n";
    $body .= "Message: $description\r\n\r\n";
    
    // Message Body - Optional Attachment
    $fileNames = $_FILES['supportingFiles']['name'];
    $tmpNames = $_FILES['supportingFiles']['tmp_name'];
    $fileErrors = $_FILES['supportingFiles']['error'];
    $fileSizes = $_FILES['supportingFiles']['size'];
    
    // Loop through each file using the count of names
    foreach ($_FILES['attachments']['name'] as $key => $filename) 
    {
        // Check if file was uploaded without errors
        if ($_FILES['attachments']['error'][$key] == UPLOAD_ERR_OK) 
        {
            $file = $_FILES['attachments']['tmp_name'][$key] ;
            if (file_exists($file)) 
            {
                echo "Attachment: $filename" . PHP_EOL ;
                $body .= "--{$boundary}\r\n";
                $body .= "Content-Type: application/octet-stream; name=\"" . basename($filename) . "\"\r\n";
                $body .= "Content-Transfer-Encoding: base64\r\n";
                $body .= "Content-Disposition: attachment; filename=\"" . basename($filename) . "\"\r\n\r\n";
                $body .= chunk_split(base64_encode(file_get_contents($file))) . "\r\n";
            }
        }
    }    
    
    // End boundary
    $body .= "--{$boundary}--";
    
    // Send the email
    if (mail($to, $subject, $body, $headers)) {
        echo "Email sent successfully!" . PHP_EOL ;
    } else {
        echo "Failed to send email." . PHP_EOL ;
    }
}
else 
{
    echo "Server Request method is not POST" . PHP_EOL ;
}
?>

