[![Discord](https://img.shields.io/badge/chat%20on-discord-7289DA)](https://discord.gg/jUnkASC)

# Sorus

Sorus is an in development minecraft client. The point of Sorus is to provide many different modifications to better the player's experience. Sorus is also open source so anyone can view the code, and contribute to the project.

 - [Contributing](#contributing)
 - [License](#license)

# Contributing

 - [Building](#building)
 - [Guidelines](#guidelines)

## Building

First you need to get the Sorus workspace on your computer. You can do this by either cloning the repository (more experienced people), or just downloading the repository. Also, add a system environment variable with the name "MINECRAFT_HOME" and set it to your main minecraft folder.

### IntelliJ IDEA
Open the build.gradle in the workspace as an IntelliJ project.
You should see the gradle widget on the right, click on that and navigate to the setup task for the version you want to run.
![Image](https://i.ibb.co/p2cfTZb/sorus-setup.png) \
Next go to Start.java in the source folder of the version you want to run and run it.\
You should see a run configuration pop up at the top, go to the edit configuration menu and copy these settings.
![Image](https://i.ibb.co/XtGLkL0/image.png) 

## Guidelines

Due to Sorus being an open source project, guidelines need to be created to ensure consistency throughout the code base. These guidelines help keep all the code looking the same way, along with keeping it in a readable and understandable format.

### Javadoc

#### Classes
Classes should have a header containing a short description of the class' purpose.
```java
    /** 
     * A plant that contains a wooden trunk, along with branches that
     * extrude from the trunk. Uses it's leaves to convert sunlight into energy. 
     */
    public class Tree implements Plant {
    
    }
```

#### Methods
Methods should have a header containing a short description of the method's purpose. The description also should contain a @param description for each parameter along with a @return description if applicable.
```java
    /** 
     * Returns the tree height based on time.
     * @param years time to grow
     * @return the height of the tree in feet 
     */
    @Override
    public int getHeight(int years) {
        return years * this.getGrowthSingleYear();
    }
```

#### Fields
Fields do not each have to have an individual description, but groups of friends should have a header containing a short description of their purpose.
```java
    private final Color color;
    private final int initialHeight;

    /** Statistics used to determine how healthy the tree is. */  
    private float water;
    private float energy;
```

### Code Style
For specifics on code formatting (blank lines, spaces, etc.), see the already implemented classes. 

# License
```
MIT License

Copyright (c) 2020 Danterus

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```