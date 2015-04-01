# A "Bootiful" Docker Application


**NOTE**: I borrowed some examples from [my friend Chris Richardson's much more in-depth blog on running Spring Boot applications with Docker](http://plainoldobjects.com/2014/11/16/deploying-spring-boot-based-microservices-with-docker). Thanks for the wisdom, Chris!

# On Application Agility
Agility is key in today's market. Applications today need to (ahem, pardon the pun) _pivot_ on a dime. Agility informs the application development process in many ways. Applications should be small, and singly focused, so that they can be updated independent of other components in a larger system. This makes it easier to develop, deploy and iterate. Applications should also enjoy repeatable, automated, and consistent builds. There should be no friction in going from a developer's workstation to a production environment, as often as possible. (Some organizations do this many times a day, for their employee's first day, even!)

There are many ways to build consistent, portable and cloud-native applications. Spring supports [12-factor app](http://12factor.net/) recommendations like those for [backing services](https://spring.io/blog/2015/01/27/12-factor-app-style-backing-services-with-spring-and-cloud-foundry) and [externalized configuration](https://spring.io/blog/2015/01/13/configuring-it-all-out-or-12-factor-app-style-configuration-with-spring) and these guidelines make it easy to build applications that run consistently in different environments.

There is, however, a gap in the story, if we stop here: how do we keep the environment in which an application itself runs consistent? Put another way: so far we've looked at ways to move Spring applications from one environment to another, adjusting for things like different hosts and ports. But the environments themselves have remained variable. An application-centric cloud (like a   Platform-as-a-Service) offers a consistent approach to describing and provisioning applications. For the longest time, Pivotal's Cloud Foundry has supported easy description of applications using a `manifest.yml`. This manifest _declaratively_  describes the makeup of the application and the system resources (RAM, network ports, etc.) needed by the application. 

# Deploy Containers
Today, however, there's a very popular alternative in [_Linux Containers_](http://en.wikipedia.org/wiki/LXC). Linux container are not a single feature, but instead a set of features like `cgroups` in the Linux kernel that make it easy to carve out resources from a host operating system and treat them like a tiny, opaque box. The application  running inside this box behaves as though it has the run of the entire system, but it doesn't. The operating system ensures proper resource and process isolation. [Docker](https://www.docker.com/), a popular library that builds on top of the Linux container facilities, offers a _very_ compelling way to use these facilities.

[Lattice](http://lattice.cf) is a Docker-native and Docker-first distributed runtime. Applications are described using Docker files and the resultant Docker images are the currency of the runtime. Lattice will underpin Cloud Foundry.next, too. Cloud Foundry, you'll recall, doesn't know about Ruby, or Java, or Python, or anything in particular. It knows about containers, in this case, Docker containers. Traditionally, Cloud Foundry converted input applications into containers (through stagers and the buildpacks), and then ran those. This isn't a surprising concept. Netflix, for example, have talked about their principle of deploying images to keep operations simple. For a lot of organizations, these _images_ might be something like Amazon Web Service AMIs, or Apache Tomcat installations (checked into the code, no less!). With Docker, it's even easier to describe an application's runtime environment and then check that description into source code.

Docker knows how to run and isolate processes. Docker's ideal because you can run Docker on your local laptop with an almost trivial amount of resources and then take that working application and run it in production. It is the last piece of the puzzle: now you have a portable way to run an application _and_ a portable way to provision the environment in which the application runs.

# Lattice - a Distributed Runtime
It's easy to [get started with Lattice](http://lattice.cf/docs/getting-started.html) on your local machine. The installation is _mostly_ about making sure that you have the popular dev tool https://www.vagrantup.com/[Vagrant] setup. Once done, running a private PaaS-in-a-box is as simple as `vagrant up` and then installing a few CLI tools.

## A (Local) REST Service

Here's a http://spring.io/projects/spring-boot[simple Spring Boot CLI application]. It's a simple REST service. I use Groovy to keep things simple, but you http://start.spring.io[could as easily use Java].

.A simple REST service  `service.groovy`
[source,groovy]
----
include::service.groovy[]
----

You can easily run this application on your local machine using `spring run service.groovy`. This of course requires that you have the `spring` command-line tool on the local environment, as well as a JDK. To simplify matters a bit, let's turn the service into a regular Java `.jar`: `spring jar service.jar service.groovy`. You should now be able to run the service using just `java`: `java -jar service.jar`.

Spring Boot makes it dead simple to normalize external configuration and treat it (from within the application) in the same way you would internal configuration files. Spring Boot responds well to numerous configuration keys and values. For example, our application runs by default on port `8080`, but it's enough to change. Just add a `-D` argument (`java -Dserver.port=9000 -jar service.jar`) or set an appropriate environment variable (`export SERVER_PORT=9000`) and then run it again and verify it by opening up `http://127.0.0.1:9000`. If you want to see what other properties a Spring Boot application will respond to, make sure to add the `spring-boot-starter-actuator` module (as we are in our simple service) and then visit the `http://127.0.0.1:9000/configprops` endpoint. If you want to look at the environment (environment variables and system properties), check out `http://127.0.0.1:9000/env`.

## Running Our Service Inside of a Docker Container
Now that we've got a handle on getting a simple, configurable service up and running, let's use Docker to describe it and run it locally.

The first thing we need to do is provide a `Dockerfile`. Our `Dockerfile` is straightforward:

.`Dockerfile`
[source,text]
----
include::Dockerfile[]
----

Docker is runs _images_ (a logical notion of an operating system and an environment) inside of _containers_. An image is made of _layers_. Docker images can _reuse_ other Docker images and treat them as a _base_ on which other specializations may be made. Our application will run on an Ubuntu Linux environment that has Oracle's Java 8 SDK installed on it. We don't need to respecify that every time. Instead, we can lean on existing images in a Docker registry (the central and default is http://dockerhub.com[Dockerhub.com]) and then only specify that which is unique to our application. Our application uses the image named `oracle-java8` from the user named `java`. This is one of the keys to the power of Docker: layers may be shared and reused. They can be _composed_ and _linked_ to each other.

The rest is pretty straightforward: `EXPOSE` tells Docker to make the port `8080` available to running Java process inside it. This does _not_, however, expose the port in the box  (the _guest_) to the outside operating system (the _host_). The `CMD` line tells Docker to call `java` and run our little service. The `ADD` line tells Docker what external artifacts to _install_ into the system's file system so that it may be in the correct state when running the `java` command.

It's easy enough to build a local Docker image.


.`build.sh`
[source,text]
----
include::build.sh[]
----

This simply stages a working directory and then creates a local _image_. The image name is `bootiful-docker`. You can then run it by that handle: `docker run -p 8080:8080 bootiful-docker`. The `-p` argument is what maps the _guest_'s port `8080` to the _host_'s `8080`. In another shell, you can see the running process by issuing `docker ps`. Bring the application up in your browser by visiting the host of the running Docker image. If you're running on Linux it'll be your localhost. If you're on an alternative host operating system then you might be using something like Boot2Docker which in effect runs Docker _inside_ of a Linux virtual machine (powered by VirtualBox), so remember to note down the host on which that's running. On my machine, the application is now available on `http://192.168.59.103:8080/hello`.

## Taking Our Application to the Cloud with Lattice/Diego/CF

You'll need a Docker registry to host your Docker image, first. There are some good, open-source, private Docker registries and https://blog.codecentric.de/en/2014/02/docker-registry-run-private-docker-image-repository/[it's easy enough to instal them].  For our purposes, however, we'll just use Dockerhub which lets you host as many images as you want (if they're public; private repositories cost money).

Create an account if you don't already have one and create a new repository. In my case, I named the repository `bootiful-docker`.

Then, we need to tie the local image to the remote repository on Dockerhub. The easiest way to do that is to _tag_ the image, like this:`docker tag bootiful-docker starbuxman/bootiful-docker`. My user is `starbuxman` and I want to link my local image (`bootiful-docker`) to the remote user (`starbuxman`) and remote repository (also called `bootiful-docker`, but it doesn't have to be).

Once that's done, it's dead simple to  upload it: `docker push starbuxman/bootiful-docker`.

Now you can run it with `diego-edge-cli start lattice-app -i "docker:///starbuxman/bootiful-docker" -c "/data/run.sh"`.
