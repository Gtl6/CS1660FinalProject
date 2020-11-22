# CloudComputingFinalProject

Tour of the project: https://pitt.box.com/s/whp39c1g5so7t1ge5ea7wrxmvx8qtu35

Text version:
I have three programs that execute on GCP:
inverted-index (located in inverted-index-src)
MR Search (located in the MRSearch folder)
Top-N (located in the Top-N folder)

Each of these programs must be compiled using the method shown in class, using the extra hadoop jars you provided. 
If you don't want to do that, the jars that are in use on the server are also in the folders with their source code.

The local program is located in docker-src. You'll notice there are several text files in an INPUT_FILES folder. That's because in my 
Dockerfile, I include the line "COPY INPUT_FILES/ /usr/src/myapp" which copies all files from the local directory onto the Docker machine.
If you want to add a file, simply add it to this folder.

The my-app folder is a standard Maven project structure, which holds the code as well as a pom.xml that allows me to fetch the Google libraries.
To execute a maven build, simply double-click the BUILD_APP.BAT. There's nothing tricky going on there, it simply runs "maven package" and then 
gets the file out of the project structure for convenience sake.

Finally, there's "Java-App-1.0-SNAPSHOT-jar-with-dependencies.jar". I recommend tab-completing when you run it. It's the exact same jar generated
by the maven build. 

To run the app on Docker, there are a few things to make sure of first. I'm assuming you're testing on Windows but I don't think I have any platform
dependent code. I just can't guarantee because I don't have a Mac to test on.

1) You're running Docker. 'docker --version' for me returns "Docker version 19.03.13, build 4484c46d9d"
2) If you want to recompile the jar, you'll need Maven. 'mvn --version' for me returns "Apache Maven 3.6.3" There's no need to though. I recompile in my video to prove there's no trickery going on there.
3) You have Xming installed. I have the most recent Public Domain Release, which is 6.9.0.31
4) You know your IPV4 address. You can find it by typing "ipconfig" into your command prompt.
5) You have entered your IPV4 address into the x0.hosts file (as described in this page https://docs.microsoft.com/en-us/archive/blogs/jamiedalton/windows-10-docker-gui)
6) Xming is running. If you're not sure (it isn't obvious for me), you can run 'XLaunch' and be reasonably sure it's running.


Now that all that's taken care of, there are only three more steps. First, open up a powershell and navigate to the docker-src folder. Then:
1) Simply paste this line into the powershell - it builds the docker image on your computer:  
		docker build -t my-java-app .
		
2) Then you can copy this command in, but you'll have to change <IPV4_address> to your IPV4 address:   
		set-variable -name DISPLAY -value <IPV4_address>:0.0
		
3) Now you can just copy this command in. It will launch docker and the application, and you should be free to engage with the program:  
		docker run -it --privileged -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix my-java-app
