# UltraKiss

**UltraKiss 4.2** is now released to provide corrections for visual editing of KiSS sets.   

Press this button to run UltraKiss.  
[![UltraKiss](https://github.com/kisekae/UltraKiss/blob/master/src/Images/ultrakissbtn.svg)](http://www.wmiles.com/ultrakiss/)

**UltraKiss 4.0** is a new release to enable online browser operation of UltraKiss as a webswing server application.  
**UltraKiss 4.1** now provides online visual editing and saving of KiSS sets using the UltraKiss IDE with Webswing.  
**UltraKiss 4.2** corrects errors with ambiguous cel offsets when visual editing KiSS sets.   

Webswing service is no longer available due to licensing issues.  A replacement service using the Jetty server is currently under development.

Use the **UltraKiss Portal** to show links to KiSS sets available on OtakuWorld and other sites on the Internet. You can also use the **File-Open** command to open and load KiSS files stored on your computer.

## History
In 2002 I developed a computer program to implement the Kisekae Set system, KiSS, a Japanese graphics system originally developed to facilitate costume changes on virtual dolls.  KiSS is a presentation method that allows the artist to program certain actions as you interact with images and other items in the application.  The original KiSS specifications were presented as a graduate thesis in Japan in 1992. 

The development of KiSS sets is an artistic process that uses images and other related elements to create new interactive entertainment. The creative process allows the artist to think in terms of visual objects and related actions applied to these objects. Unlike "computer art" which creates or displays traditional art via a computer, KiSS uses the computer as the medium, allowing the art to be not only animated, but also interactive. 

UltraKiss was developed as a platform independent system to help artists build their KiSS sets. UltraKiss can be used to view KiSS sets and provide an integrated development environment to construct KiSS sets. UltraKiss provides an integrated color editor tool to change the colors of images.  An image editor tool lets you change the size, shape, or structure of your images.  There is also an integrated audio and video player so that you can include different sounds or movies in your model.  For packaging the work, UltraKiss has an integrated LHA, ZIP, and JAR archive file manager that lets you convert between archive types and save your KiSS set in standard files or compressed archive files. 

Interactions between objects are recognized through an event processing language for object interaction known as FKiSS.  This language facilitates the programming of automated actions and system interaction with the user. This language was originally conceived as a non-procedural language that would respond to various events, but with the development of FKiSS 3 a procedural structure was imposed on the language that introduced program variables and control logic.

UltraKiss extended and standardized many aspects of the FKiSS language. The standardization, referred to as FKiSS version 5, was designed to correct many of the FKiSS language deficiencies. Each UltraKiss FKiSS command is either a backward compatible extension to previous versions of FKiSS or is a new command implemented for completeness or new capabilities or is an UltraKiss viewer specific command.

Some examples of interactive KiSS sets are shown below.


#### Example KiSS Sets

![NoGodsLand - Mindy's Secret Place](https://github.com/kisekae/UltraKiss/blob/master/src/Splash/MindySecretPlace.png)

![A Slot Machine](https://github.com/kisekae/UltraKiss/blob/master/src/Demo/slotmachine.jpg)

![FreeCell Card Game](https://github.com/kisekae/UltraKiss/blob/master/src/Demo/freecell.jpg)

![Sailor Moon](https://github.com/kisekae/UltraKiss/blob/master/src/Images/intro_03.jpg)



#### How does UltraKiss work?

UltraKiss reads and interprets text based KiSS set configuration files contained within archive files or file directories.  The archive files or directories contain computer graphics images, color palettes, sounds, and other related control files.  Japanese Kisekae sets as conceived in the original graduate thesis were often drawings of anime characters with many different costumes packaged in an LHA archive file for viewing the work. Kisekae became popular in Japan and was adopted by the western world. Objects in the original KiSS model such as a doll figure, costumes, and other items are constructed from layered image components that usually show only parts of the complete image. 

The original KiSS specification defined one image format, known as CEL, to represent image pixel data. With CEL images, the image data is isolated from the palette data.  A CEL image does not contain a color table as part of the image file, therefore a separate color file must be referenced in order to properly display the image. This is known as a KiSS Color File, or KCF file. Because the separation of colors from data introduced management difficulties for keeping track of pixel data and color data, CEL images have not become a widely recognized image format.

UltraKiss, as part of the FKiSS 5 specification, enabled standard image types such as GIF, JPG, PNG, and others to be used as image cels.  UltraKiss also implemented standard graphical user interface components such as buttons and lists and other forms as image cels for used input and display presentation.  These features facilitate the development of more complex applications from graphical images.

When KiSS was originally conceived an object was considered to be a collection of cel image files grouped together as a unit. In KiSS, the term object is an abstract term that relates a number of entities to a particular point on the screen.  In practice, an object group is the realization of this abstraction and is a concrete implementation of a number of image cels in one movable group that is visible on the screen. An object can be dragged by the mouse and manipulated by the user as a single entity. 

Objects can also be manipulated through the FKiSS event processing model that enables timed animations or other actions if objects are touched or dragged across the screen. Color variations, enabled through palette changes in UltraKiss, can also be used to produce different results. 

An example of a KiSS configuration file using graphic input and display components with code logic to compute the factorial of a number is shown below.

![N Factorial Calculator](https://github.com/kisekae/UltraKiss/blob/master/src/Demo/factorial.jpg)

![Factorial CNF File](https://github.com/kisekae/UltraKiss/blob/master/src/Demo/factorial.png)


In 2008 I ceased this work. Computers were faster and the KiSS model was replaced with different animation tools.

In 2018 I removed this application from my websites. Later, I had a request to restore the application.

In 2023, prompted by new KiSS work from [The Owl](http://followtheowl.com/) with his Scarecrow sets and NoGodsLand interactive quest game, I completed this project.  I am releasing my source code under a GNU version 3 license in the hopes that it is useful and brings enjoyment to others.  I am seeking contributors and another sponsor to take over this work.


### How to get started with UltraKiss


1. Download the current UltraKiss release from [GitHub](https://github.com/kisekae/UltraKiss/releases).  For automated installation, see the release downloads.

2. For manual installation, extract the files from the **UltraKiss_4.2.zip** (or later) download. This extract will create a folder named **ultrakiss**.

   For local execution on a Windows system with or without Java installed, run **UltraKiss_4.2_jre8_201.exe** found in the **ultrakiss** folder. This program is a fully self-contained bundled application created with [Launch4j](https://launch4j.sourceforge.net/).

   For local execution on a Linux or Apple system with Java already installed, run the **kisekae.jar** file from the **ultrakiss** folder using the **java -jar kisekae.jar** command.

   If the **.jar** extension is configured in your system as a Java application then you can run the program by directly clicking on this file.

   On all systems, you must run the UltraKiss program from within the **ultrakiss** folder. This is required for access to the sound and help libraries. This folder also contains a standalone version of the Java Runtime Environment (JRE) version 8 for systems that do not have Java installed.

   

   ![](https://github.com/kisekae/UltraKiss/blob/master/src/Images/UltraKiss.jpg)

   

3. Use **Help-Contents** to access the UltraKiss program documentation. See **Help-Contents-What is UltraKiss** for a brief description of the Kisekae Set system.

   Use **Help-Demo Sets** to access the KiSS demonstration sets that are packaged with UltraKiss. Click on any of the listed sets to load the set in UltraKiss. To see the FKiSS code behind any KiSS set press F11 or use the **View-Active Configuration** menu command.
   
4. To learn how to make KiSS sets of your own explore the tutorials provided with UltraKiss. See **Help-Tutorials**.  In particular, look at the FKiSS tutorial to learn how to use the UltraKiss FKiSS Editor debugging tool to breakpoint and step through your code execution.
   
5. Explore the reference documentation provided with UltraKiss.  Examine the **UltraKiss Introduction** section and the **FKiSS** section.

   The FKiSS event and action statement documentation describes the FKiSS language and provides illustrative examples to help you develop your own interactive KiSS sets.




### How to contribute to UltraKiss

Contact me through [GitHub](https://github.com/kisekae) or my personal website [Bronze Art by William Miles](https://www.bronzeart.ca).  I am seeking to transition this project to another contributor.

 

### Basic KiSS Features

- Layered objects, so the front and back of doll clothes can slide over the doll in a realistic way.             
- Different page sets or scenes, where each page can show new sets of clothes or background pictures.                    
- Multiple color sets, so that you can view your doll clothes and scenes in different shades of color.                    
- 'Sticky' objects, so that some items may have to be tugged before they will move freely.                  
- Object events, so that doll features, hairstyles, or clothes can change when touched.                    
- Semi-transparent images, so you can see through them to the objects below.                                    

### Enhanced Features

- A complete editing module for selecting, copying and pasting objects on pages.                                            
- A comprehensive programming model for developers to create active content.        
- A fully integrated text editor for developing KiSS data set configuration files.   
- A fully integrated image editor to enhance and touch up KiSS image files.    
- A full featured color editor for maintaining and updating KCF image palette files.        
- An integrated archive file manager for creating new LZH and ZIP archive files.  
- An integrated media player for playback of MP3, WAV, AU, MIDI audio and video files.    
- Support for loading KiSS sets packaged in ZIP and LHA compressed files.     

### KiSS File Support

- KiSS 16 and 256 color CEL file formats.  
- Cherry KiSS truecolor CEL file formats.     
- KiSS 16 and 256 color KCF file formats.    
- GIF87 and GIF89a graphic file formats.                 
- JPEG photographic image file formats.            
- PNG file formats with translucency.           
- BMP graphic file formats.      
- PBM, PGM, and PPM graphic file formats.        
- WAV, AU, and MIDI audio file formats.    
- AIFF, MP3, and RMF audio file formats.         
- ANSI text and rich text RTF file formats. 
- ZIP, LHA, and JAR compressed files.    

### FKiSS Specification Support

- FKiSS 1, FKiSS 2, FKiSS 2.1, FKiSS 3 and FKiSS 4 specifications.      
- Unrestricted length alphanumeric variable names can be used.  (FKiSS 5)       
- String and integer variable types are supported.  (FKiSS 5)
- Variable names can be used to reference KiSS objects.  (FKiSS 5)   
- Indirect references to variable values is supported.  (FKiSS 5)      
- Indexed variables can be used for loop control.  (FKiSS 5)           
- For statements and while loops can be used for iteration control.  (FKiSS 5)  
- Nested if-else-endif logic can be coded for complex algorithms.  (FKiSS 5)               
- Local variable scope for label events supports recursive operation.  (FKiSS 5)      
- Label parameters and return values enable function calls.  (FKiSS 5)                                

### New UltraKiss Programming Extensions (FKiSS 5)

- "letcel" command to identify the currently selected image cel.    
- "letcomment" command to obtain the cel comment text.          
- "while" and "endwhile" commands for program loop control.           
- "for" and "next" commands to perform iteration.                 
- "showstatus" command to display text in the program status bar.             
- "concat" command to concatenate string text.                  
- "substr" command to obtain a substring of a string.         
- "replacestr" command to replace a substring in a string.                
- "indexof" command to return the position of a substring in a string.   
- "strlen" command to return the length of a string.  
- "clone" command to dynamically create new object groups.       
- "destroy" command to remove cloned object definitions.        
- "animate" command to suspend or restart cel animation.    
- "mediaplayer" command to queue background music.  
- "lettimer" command to return the time remaining on an alarm.  
- "letpal" and "setpal" commands to set a specific palette group for an image cel or object.  
- "letkcf" and "setkcf" commands to adjust the ordinal palette file for an image cel.     
- "confirm" command to show a confirmation dialog and wait for a response.           
- "setmodal" and "letmodal" commands to restrict user events to a specific object.    
- "read", "write", "open" and "close" commands for external file access.       
- "viewer" command to access viewer specific control features.       
- "environment" command to access external information and objects.    
- "signal", "wait", and "sleep" commands for multiple activity synchronization control. 
- "paint" command to force a redraw of the current screen.                                

### GUI Components (UltraKiss Extension)

- Support for basic Graphical User Interface components for simplified user input.     
- Labels, Buttons, Check Boxes, Lists, Text Fields, Text Areas, Radio Buttons.        
- Individual component attributes to control font characteristics, color, scrolling, and state.        
- Integration with the FKiSS event model to recognize user component events.            
- Ability to replace the viewer menu bar with application defined menu items.   
- "getText" and "setText" commands to set and retrieve component text values. 
- "getSelectedValue" and "setSelectedValue" commands to manage list components.       
- "getSelectedIndex" and "setSelectedIndex" commands to manage list entries.       
- "getSelectedItem" and "setSelectedItem" commands to manage combo box entries. 
- "getSelected" and "setSelected" commands to manage button entries.   
- "getValueAt" and "setValueAt" commands to manage list entries by index.
- "addItem" and "removeItem" commands to construct list and combo box components.          
- "getIndexOf", "getItemCount", and "removeAll" commands to manage list entries.                                    

### Usability Enhancements

- KiSS sets may be loaded from compressed archives or file directories.                                            
- Expansion data sets can be seamlessly loaded over previously loaded data.                                            
- Support for extended directory path information in compressed archive files.                                            
- Animation thread control interface to manage event processing threads.                                            
- Ability to create an unrestricted number of page sets and color sets.                                            
- Complete undo and redo control over most data set modifications. 
- Cut, copy and paste of graphic objects within and between  pages.                                            
- Restart option to initialize the KiSS data set and start all events. 
- Reset option to restore all scene objects to their initial positions.
- Size to Screen option to resize page sets to the available viewing space. 
- Scale to Screen option to scale all images to fit the viewing space.    
- Magnification and reduction and scaling of all images on the screen.
- Visibility of all cel, palette, page set and programming event definitions.                                            
- Event logging and diagnostic breakpoints facilitate program debugging.                                            
- FKiSS visual code tracing and event program source code editing.                                            
- Preview image magnification, selection, and positioning during color edits.                                            
- Feature to write current object positions to a new configuration file.                                            
- Facility to adjust image draw level depth and layering order.
- Ability to import standard image formats and convert to Cel format.                                    

### UltraKiss Portal Features

- Ability to automatically download and open KiSS sets from the internet.       
- Display of HTML 3.2 specification pages with images, frames, and headings.   
- Forward and Back controls to traverse the navigation history.                                            
- Automatic authentication to secure web sites using a registered user identifier.           


![](https://github.com/kisekae/UltraKiss/blob/master/src/Help/product/fkisseditor/images/intro_02.jpg)



### Questions and Answers

​           

####       **How do I create a brand new KiSS set?**    

​      Close any loaded set to ensure that UltraKiss displays the main logo screen. Use the **File-New** command to create a new KiSS set. This will automatically create a new configuration file (CNF file) in the KiSS set and the new configuration will be assigned a new sequential name. One new empty page set will be created for you.    

​      You must now import images into this page using the **Edit-Import** command. You can also edit the configuration file manually to construct your KiSS set. You can use the UltraKiss edit functions to position images on the page, adjust their layering order, or otherwise develop your KiSS set. For further information see the tutorial document that provides hints and advice on constructing KiSS sets.    

​      Remember to save your new KiSS set. You may rename the configuration element when it is saved.    

​           

####       **How do I create a new, different configuration file for my data set?**    

​      With your KiSS set loaded into UltraKiss, use the **View-Active Configuration** command to open a text editor window for your existing CNF file. Save the file under a different name to create a copy of your configuration.     

​      If you save the file with a CNF extension the new file name will show up in the **File-Select** command configuration list.  Select the new configuration and load it into UltraKiss.  You can now use UltraKiss to import new images or edit the configuration as required. All edit changes will now be applied to the new configuration element.    

​           

####       **How do I load expansion sets into UltraKiss?**    

​      Load your original or base KiSS configuration file into UltraKiss with the **File-Open** command. Then, load your expansion set with the **File-Add** command.     

​      Expansion sets are loaded on top of the currently loaded base set. They add new images and other objects to the base set.  Any image cel or palette or audio object referenced in the expansion set CNF file but not found within the expansion set itself is referenced from the base set. Once the expansion set is loaded the base set is dropped. The new expansion set and all objects referenced in the expansion configuration file become the new base set.  If multiple expansion sets are loaded each builds upon the last. 

​      UltraKiss does not know that a set is an expansion set. If an expansion is loaded on the wrong base set many errors will occur. This is why the 'cancel' option exists. If the expansion load is cancelled the original base set should still be available.    

​           

####       **How do I package my KiSS set into an LZH file?**    

​      If your KiSS set elements are all contained within a standard file directory you can use the Archive Manager tool create a new LZH file or ZIP file and add all your KiSS data set elements from your directory into this file. If you have segregated your data set into subdirectories remember to set the 'recurse folders' and 'save relative path' options to add all the subordinate files, too.    

​      You can also load your set from its directory and use the **File-Save As Archive** command to save all set KiSS elements in a new archive file.     

​      If your KiSS set is already packaged into an archive file, then you must open this archive file in the Archive Manager and copy it or save it to a new file name with the LZH or ZIP extension.    

​           

####       **What is the difference between a grouped object and an ungrouped object?**    

​      Grouped objects and ungrouped objects are terms applied to selection sets when using the UltraKiss editing features. In KiSS, the term 'object' is an abstract term that relates a number of entities to a particular point on the screen.  In practice, an 'object group' is the realization of this abstraction and is a concrete implementation of a number of image cels in one movable group that is visible on the screen.    

​      When visually editing your KiSS set you will sometimes work with the object groups and you will sometimes work with the individual image cels. If you are editing an object group, then this group is a collection of entities such as cels. The selected object group is called a 'grouped object'. This group must be broken up into its individual components if you want to work with the actual image cels. When the group is broken up it is called an 'ungrouped object'.    

​      The **Edit-Ungroup Object** command breaks all selected object groups apart.  You will then have a number of individual image cels that can be separately positioned or cut or copied or pasted as required. These individual image cels can be regrouped back into their original object groups or combined into a new object group by using the appropriate edit command.    

​           

####       **What is the image offset and how is it used?**    

​      The image offset is an attribute of every image in the KiSS data set. The image offset is a displacement from the image location. This offset is used to calculate the bounding box that describes the area of the screen that needs to be repainted when the image is drawn.    

​      Image offsets are used to position image cels within object groups. Every object group has a location on the screen and each image cel within the group is initially located at this object group location. The image offset is used as the displacement from the object group location to identify where the image cel must be drawn within the object group.    

​      Image offset values are stored as internal attributes of CEL files and GIF files. Offset values are not stored internally for JPG, BMP, or PPM files. Thus, if you save an updated KiSS set and write the images to new files, the actual offset values in use will be written inside GIF and CEL image files, and written as UltraKiss %offset flags on the configuration object lines for all other image types. If you replace a GIF or CEL file outside of UltraKiss with an old copy of the  image you can lose all positioning offset changes that UltraKiss has made for you through your previous editing operations.    

​           

####       **How does UltraKiss process FKiSS events?**    

​      UltraKiss uses multiple event handler threads or activities to process events. There is one master event queue and each event handler scans this queue every N milliseconds looking for work. If an event has been queued, such as a press event or a keystroke event, then the event is captured, fired, and executed. During event execution the capturing event handler is busy. If another event is queued at this time then a different event handler thread can capture the new event and process it. Thus, on a multi-activity system we can have two or more event handlers running concurrently.    

​      Each FKiSS event is a single block of action commands. Each action statement in the block is processed in sequence.   Sequential statement flow can be altered through the use of 'gosub'       actions or 'goto' actions, or loop control statements such as 'for' and 'while'.    

​      The 'gosub' and 'goto' actions refer to label events. A label event is an event that contains a new set of action commands. 'Gosub' and 'goto' actions directly fire a label event without queuing it, thus the label event runs under the current event handler thread. A 'gosub' action recursively fires a label event so that it will return to the correct call point and a 'goto' action jumps to the beginning of the label event as no return is required.  All actions within a label event are treated as a separate block.    

​      All loop control statements must be consistent within the same event block. A 'for' statement in one block cannot match with a 'next' statement in a different label block.  An 'if-else-endif ' sequence must be fully contained within one label block.    

​           

####       **How does the UltraKiss timer action work?**    

​      Timer actions set alarm event states. An alarm is an event that can contain a series of action statements. A separate activity monitors all alarms and will queue an alarm event when the alarm time period has expired.     

​      With multiple event handlers, if a short timer period was set the alarm could fire immediately and the alarm event could run concurrently with the scheduling event. This behaviour seemed incompatible with coding practices that assumed timer actions were not immediate, as would occur with a single threaded viewer implementation. To maintain compatibility with other KiSS viewers UltraKiss disabled firing alarms referenced by the event timer actions until the primary scheduling event terminates.    

​           

####       **Timer action statements do not seem to be completely time accurate. Why is this?**    

​      The UltraKiss timer is a polled timing implementation. The alarm queue is polled on a periodic basis looking for alarms whose time has expired. For UltraKiss timing to be accurate a timer action command period must be an exact multiple of the timer period seen in the **Options-FKiSS-Timer** setting. If the timer action command programmed delay is not a multiple of the timer period then the timer must wait for the next cycle to fire.     

​      The default timer period is 10 milliseconds. It is not constructive or possible to set a smaller value.    

​      For accurate timing ensure that all timer action statement values are exact multiples of the timer period.    

​           

####       **Why can I not move images within the Color Editor preview window?**    

​      The image preview window does not let you to pick up and move images in the window. (The preview window is not an UltraKiss viewer). The move pointer simply lets you adjust the location of the image in the window but only if the image exceeds the window size. This would be the case if a portion of the preview image was magnified or zoomed.    

​           

####       **How do I change the color of an image or article of clothing?**    

​      You use the color editor to change image colors. The best way to change the color of an article of clothing is to first select a pixel in the preview image with the eyedropper, then select all 'similar' colors using the Hue button. The sensitivity of the Hue button is controlled with the little up-down arrows in the corner of the button. As you increase the sensitivity value more and more colors from the palette will be chosen.    

​      Once you have the colors selected, switch to the HSB panel of the color chooser and drag the slider to a new hue. The preview image will change to show the new colors. Pixel hues are adjusted based upon their relative differences from the first, or active color that you selected with the eye dropper. Dark colors remain dark and light colors  remain light.    

​      It is quite easy to change overall tint in this way. For example, try changing the color of an image from red to blue.    

​      If the results in the preview window are not what you want then simply undo the color change. If you missed selecting all the necessary colors, simply increase the sensitivity on the Hue selection button and try again, or pick up a different colored pixel from the preview image and use the Hue button to select a different set of 'similar' colors. Best results occur if you initially select a middle-of-the-road color for the active color.    

​           

####       **What happens when colors are changed in the Color Editor? Some colors change in unexpected ways.**    

​      When new colors or tints are applied to the image relative adjustments are made to color hue, brightness and saturation.   If you use the color chooser and significantly darken or lighten differences from the active color then some related colors in the selection set may become 'saturated', or reach their limit of change. If necessary, you can turn off relative brightness, saturation or hue changes by right clicking on the appropriate toolbar button to disable it. You can also turn off relative color adjustments through the Options menu. If this is done all selected colors will be set to the specific active color.    

​      The color editor is designed to change hue. The  identical techniques will work to change the brightness, saturation, red, green, or blue color components of the image.     

​           

####       **When I edit the colors of a KiSS cel, colors change in other cels, too. Why does this happen?**    

​      More than one KiSS cel can use the same KCF palette files for colors. If you edit a KCF file then all cels that use this KCF will change.     

​      There is an 'All Cels' option in the image cel drop-down box that can be used to concurrently draw all images in the preview window for an active KiSS data set. If a color change is made to one specific cel then this option is helpful to see the impact on all other cels that use the same KCF file.     

​      This option does not exist for external palette files loaded with the color editor **File-Open** command.    

​      If you are editing an active configuration and are satisfied with the color edit changes, then the changes can be applied to the active KiSS data set when you exit from the color editor. If the color changes are now seen to be unsatisfactory, you can easily undo the KiSS data set changes with the **Edit-Undo** function, and try again.    

​           

####       **How do I save a GIF (or BMP or JPG or PPM) file as a CEL file?**    

​      GIF or BMP or JPG or PPM files can be loaded into either the color editor or the image editor tool using the **File-Open** command. They can be saved with the **File-Save As** command as a CEL file by changing the file name extension. You can also load recognized image files using the UltraKiss **File-Open** command. This will automatically load the file into the image editor, and then the file can be saved. Remember, you must change the file name and specify the CEL file extension on the **Save As** dialog to write a CEL file.    

​      If a palette type image is converted to a CEL then an associated KCF file will also be saved. The KCF file will have the same name as the CEL file. GIF or BMP palette type images are saved in this way. JPG, PPM and BMP images that are truecolor images will be saved to Cherry Kiss type CEL files. These files do not have an associated KCF palette file.    

​           

####       **How do I save a CEL file as a GIF or BMP or JPG or PPM file?**    

​      If a standard palette type CEL file has been loaded into the image editor or color editor, then you can save this file as a GIF or BMP or JPG or PPM file with the **File-Save As** command. You must change the file name and specify the required file extension on the **Save As** dialog. Palette type CEL files can be saved as any type of supported file.     

​      If truecolor images such as Cherry Kiss cels, JPG images, BMP uncompressed images, or PPM images are loaded into the image editor or color editor, these are saved as GIF files by dithering to 256 colors.    

​           

####       **How do I create a new KCF palette file from a CEL or GIF or BMP or JPG or PPM image?**    

​      Load the CEL or GIF or BMP or JPG or PPM image into the image editor or color editor. Select the **File-Save As** menu command and change the file name extension to KCF. A KCF file will be written if and only if the image does not contain more than 256 colors.    

​           

####       **How do I reduce the number of colors in a JPG or other truecolor file so that it can be saved as a GIF file?**    

​      Both the Color Editor and Image Editor tools contain menu items that will allow you to set the maximum number of colors in a file. Images are dithered to ensure that the resulting image does not contain any more colors than the specified maximum.    

​      GIF files can maintain a maximum of 256 distinct non-transparent colors. CEL files can maintain a maximum of 255 distinct non-transparent colors.     

​           

####       **How do I add multiple palette groups to a GIF file? I add them in the color editor but they are not saved.**    

​      You cannot save multiple palette groups to anything other than a KCF file. Other image files such as GIF files do not directly support multiple palettes.     

​      The color editor will let you create new palette groups for any loaded palette, but currently cannot save these to anything other than KCF files. The feature to support multiple palettes for other image files may be added in a later release.      

​           

####       **How do I paste images into UltraKiss from other programs?**    

​      UltraKiss supports direct image transfers (copy and paste) from other programs with a Java 1.4 run time virtual machine.  You can import copied images from some external programs into UltraKiss using the Edit-Import Image command.     

​      To transfer an image from an external program into UltraKiss, use your external program to save the image into a GIF, BMP, JPG, or PPM format file supported by UltraKiss. Then import the image into UltraKiss using the **Edit-Import Image** command.    

​           

####       **When I save a file UltraKiss writes it to an archive. How do I save the file to a file directory?**    

​      If your data set was loaded from a compressed archive file UltraKiss will write all updated files back to this archive. If you want to work with separate files, use the Archive Manager to extract all files from the archive into a directory of your choice. Load your KiSS configuration file from this directory.  All saved files will now be written back into this directory.    

​           

####       **How are movies implemented in UltraKiss?**    

​      Movies are image cels. They are very similar to GIF animated cels. You define a movie cel in the KiSS configuration file as you would any other image cel.     

​      Because movies are cels, they can be used in the same way as other image cels. They can be picked up, moved, mapped, unmapped, and so on. Movies differ from normal image cels in the following ways.    

- Movie cels are not visible unless they are playing. Visible movies are always displayed on top of all image cels. It is not possible to overlay a movie with another image. You start and stop a movie with the FKiSS movie() action command. This will make the movie visible.
  
- Movie cels cannot be made transparent.       

​      Movie cels do not begin playing when the cel is mapped, or made visible. However, this behaviour may be implemented in a future release.    

​           

####       **How does object collision detection work in UltraKiss?**    

​      Objects collide when they touch. A touch occurs when two visible pixels overlap. The FKiSS events in(), out(), stillin(), stillout(), apart(), collide() and their related action commands all examine whether or not the objects touch.    

​      Collision events can occur whenever objects are moved, either through dragging with the mouse or any related FKiSS move action command. Collision events are fired when the object stops moving. This happens on a mouse release if the object is being dragged, or upon completion of the move command if the object is being moved through an FKiSS action. The event is not fired during the move, so transitory inter-object collision events are not recognized if the object happens to pass over or through some other object.    

​      If a sticky object group is moved collision events are not detected. Sticky objects snap back to their original position. They are considered not moved. Any movement event that has a zero displacement from the object start position does not fire collision events.     

​      Collision events happen when objects are moved with an FKiSS action command or manipulated with the mouse. Collision events can also occur when object visibility is changed, as could happen if a previously unmapped object was made visible over top of another object.     

​      UltraKiss uses an asynchronous event processing model and there is no defined collision event execution order.    

​      The events in(), out(), stillin() and stillout() check whether their object group image bounding boxes touch or overlap.  Object groups can contain many images. When two object groups overlap there must be at least one image from each object group whose image bounding boxes have a non-empty intersection. For object groups to touch their contained images need not be visible or mapped but they must contain image cels that are defined to be on the current page.    

​      The apart() and collide() events check for overlaid images. These events examine image pixels looking for non-transparent pixels that overlap. A non-transparent pixel is any pixel that is not completely transparent. For image cels to touch they must be visible on the current page and their image bounding boxes must have a non-empty intersection.     

​      The same image cel can exist in more than one object group. Thus, it is quite possible to specify an FKiSS event that specifies the same image name for each argument, such as       collide("image.cel","image.cel"). In this case the collision checks for image overlap between different copies of the image cel in different object groups.    

​      Collision events are symmetric. An event such as in(#1,#2) is equivalent to in(#2,#1). A collision between object 1 and object 2 is the same as a collision between object 2 and object 1.   UltraKiss ensures that the symmetric event is always recognized so it does not need to be coded.    

​           

####       **In what sequence are FKiSS events processed?**    

​      UltraKiss uses a multi-activity asynchronous event processing model. Events are queued when recognized and can begin execution immediately on a multi-activity system if resources are available. There is no guarantee of a specific event execution order.     

​      For user initiated actions the mouse down signal will occur first, followed by a series of mouse drag events, and then the mouse up signal. UltraKiss queues FKiSS press(), catch(), and fixcatch() events on a mouse down signal. FKiSS drop([)](reference/fkiss4.html#drop), fixdrop() and unfix() events can be queued at any time on mouse drag actions. The FKiSS collision events in(), out(), stillin(), stillout([)](reference/fkiss2.html#stillout), apart([)](reference/fkiss2.html#apart) and collide() are queued on a mouse up signal, followed by necessary drop(), fixdrop(), release() and unfix() events.     

​      When a KiSS data set is initialized UltraKiss processes, in sequential order, any initialize() events, any version() events, then any begin() events. During initialize() and version() no action command related events are recognized or queued. For example, a changeset() command in initialize() will not initiate set() events for the page.     

​      Event handler activities are started before the begin() event.  During begin(), action command related events and alarms are recognized and queued for execution.     

​      When the begin() event terminates UltraKiss will open the last page set referenced through a changeset() command, or page set 0 if the starting page was not set. When a page set is opened or a changeset() action command is executed UltraKiss will process any set() events defined for the page. When a page set is opened an associated color set can automatically open, too. When a color set is opened or a changecol() action command is executed UltraKiss will process any col() events defined for the palette group.    

​      On a multi-activity system the FKiSS programmer cannot assume that events will always execute sequentially in the order in which they are queued. Sequential execution is possible only if a single FKiSS event handler is configured. See **Options-FKiSS-Event Handlers**.        



![](https://github.com/kisekae/UltraKiss/blob/master/src/Help/product/kisekae/images/menu_37.gif)
