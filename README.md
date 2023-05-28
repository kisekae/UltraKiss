# UltraKiss
In 2002 I developed a computer program that implements the Kisekae Set system, KiSS, a Japanese graphics system originally developed to facilitate costume changes on virtual dolls.  

Kisekae is short for "kisekae ningyou", a Japanese term meaning "dress-up dolls". Unlike "computer art" which creates or displays traditional art via a computer, KiSS uses the computer as the medium, allowing the art to be not only animated, but also interactive. 

UltraKiss extends the KiSS medium to visual artistic programming. The Kisekae Set system is not restricted to dress-up dolls but is a development system to create visual applications.  This is an artistic process for computing that uses illustrations and visual elements. It allows the artist to think in terms of visual objects and actions rather than text-based code.



![Sailor Moon](https://github.com/kisekae/UltraKiss/blob/master/src/Images/intro_03.jpg)



UltraKiss is developed to help artists build their KiSS sets. UltraKiss reads and interprets KiSS data files.  These files contain computer graphics images, color palettes and data control files.  Objects, such as the doll figure, costumes, and other items are constructed from layered image components that usually show only parts of the complete image. 

Objects can be manipulated through user actions or through  a simple event processing model that enables timed animations or other actions if objects are touched or dragged across the screen.  Color variations, enabled through palette changes in UltraKiss, can also be used to produce different results.  

In 2008 I ceased this work. Computers were faster and the KiSS model was replaced with different animation tools.

In 2018 I removed this application from my websites. Later, I had a request to restore the application.

In 2023, prompted by new KiSS work from [The Owl](http://followtheowl.com/) with his Scarecrow sets, I completed my work.  I am releasing my source code under a GNU version 3 license in the hopes that it is useful and brings enjoyment to others.  I am seeking contributors and another sponsor to take over this work.



## How to get started with UltraKiss



1. Download the current UltraKiss release from GitHub.  For all systems, extract the files from the **UltraKiss_3.5.zip** download. This extract will create a folder named **ultrakiss**.

   For local execution on a Windows system with or without Java installed, run **UltraKiss_3.5.exe** found in the **ultrakiss** folder. This program is a fully self-contained bundled application created with Launch4j.

   For local execution on a Linux or Apple system with Java already installed, run the **kisekae.jar** file from the **ultrakiss** folder using the **java -jar kisekae.jar** command.

   If the **.jar** extension is configured in your system as a Java application then you can run the program by directly clicking on this file.

   On all systems, you must run the UltraKiss program from within the **ultrakiss** folder. This is required for access to the sound and help libraries. This folder also contains a standalone version of the Java Runtime Environment (JRE) version 8 for systems that do not have Java installed.

   

   ![](https://github.com/kisekae/UltraKiss/blob/master/src/Images/UltraKiss.jpg)



2. Use **Help-Contents** to access the UltraKiss program documentation. See **Help-Contents-What is UltraKiss** for a brief description of the Kisekae Set system.

   Use **Help-Demo Sets** to access the demonstration KiSS sets that are packaged with UltraKiss. Click on any of the demonstration sets to load the set in UltraKiss. To see the code behind any KiSS set press F11 or use the **View-Active Configuration** menu command.

   To learn how to make KiSS sets of your own explore the tutorials provided with UltraKiss. See **Help-Tutorials**.
   

3. Explore the reference documentation provided with UltraKiss.  Examine the **UltraKiss Introduction** section and the **FKiSS** section.

   The FKiSS event and action statement documentation provides illustrative examples to help you develop your own interactive KiSS sets.

   

## How to contribute to UltraKiss

Contact me through [GitHub](https://github.com/kisekae) or my personal website [Bronze Art by William Miles](https://www.bronzeart.ca).  I am seeking to transition this project to another contributor.

 
