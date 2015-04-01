# Lattice, a Distributed Runtime for your Containerized Spring Workloads

## Portable Applications

Spring's always made building portable applications as simple as possible. The very idea of dependency injection  provides the crucial bit of indirection required to insulate an application from an underlying platform, and there are numerous patterns that promote cloud-ready, portable, and movable applications. The [12 Factor App](http:/12factor.net) manifesto prescribes many good recommendations. We've even covered some of them, like  [backing services](https://spring.io/blog/2015/01/27/12-factor-app-style-backing-services-with-spring-and-cloud-foundry) and [externalized configuration](https://spring.io/blog/2015/01/13/configuring-it-all-out-or-12-factor-app-style-configuration-with-spring), in this very space recently.

As developers, we're used to being able to test applications in isolation, to validate the inputs into an application and validate the resulting behavior.  We're used to reproducible builds; we're used to being able to throw away the build and - with the disciplined use of tags and so on - reproduce the same build. This isn't news.

## Cattle

It took us developers a while to get to this place, but we did. In the last 8 years we've seen this rigor come to operations teams. Operations want reproducible infrastructure as much as we developers want reproducible builds. Consistency and reproducibility are even more important with disposable, ephemeral, cloud infrastructure. As Adrian Cochraft (formerly at Netflix) reminds us: treat servers like cattle, not pets.

It's 2015, things have come a long way. Developers and operations are - ideally at least - two highly integrated teams. Developers want consistency in the way their applications run, operations want consistency in their infrastructure.  

## Deploy Containers

[Linux Containers, or _LXC_](http://wikipedia.com/wiki/LXC), are a set of features in the Linux kernel designed to isolate applications. Applications run as though they've got the run of the kernel. LXC aren't, then, a single API or feature, but a set of independant ones that can be used together.  Container technologies like Docker and Rocket leverage LXC features and let you control them declaratively. Using Docker, for example, you can write a Dockerfile, check it into your code and then use that Docker file and code to reproduce both the application and its environment, and do so at a much lower runtime footprint than traditional virtualization. This makes things simpler: operations deploy containers, not applications.

The only thing that remains, then, is automation around managing, running and scaling containerized workloads. This is where a distributed runtime like [Lattice](http://lattice.cf) comes in. From the site:

> Lattice aspires to make clustering containers easy. Lattice includes a cluster scheduler, http load balancing, log aggregation and health management. Lattice containers can be long running or temporary tasks which get dynamically scaled and balanced across a cluster. Lattice packages components from Cloud Foundry to provide a cloud native platform for individual developers and small teams.

Lattice is easy [to deploy both on a proper cluster](https://github.com/cloudfoundry-incubator/lattice#clustered-deployment) or [locally using Vagrant](https://github.com/cloudfoundry-incubator/lattice#local-deployment).

## A Simple Spring Boot Application

### The Code
Let's get a simple example working. Our first example will be a basic Spring Boot CLI REST endpoint with no services in a file, `service.groovy`:  

```java
@Grab("spring-boot-starter-actuator")
@RestController
class GreetingsRestController {

  @RequestMapping("/hello/{name}")
  def hi(@PathVariable String name){
    [ greeting : "Hello, " + name + "!"]
  }

  @RequestMapping("/hello")
  def hi(){
    hi("World")
  }

  @RequestMapping("/killme")
  def goodbye(){
    System.out.println( 'goodbye cruel world!')
    System.exit( -1 )
  }
}

```

We have two endpoints (`/hello`, and `/hello/{name}`) that simply return a value, and a third endpoint (`/killme`) that will abruptly (rudely!) terminate the endpoint. To run this on your local machine, use the [Spring Boot CLI](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#getting-started-installing-the-cli):

```bash
spring run service.groovy
```

The goal is to get something we can wrap in a Docker container, and the simpler a Docker file, the better. Let's deploy a standalone `.jar`:

```bash
mkdir -p build
spring jar build/service.jar ./service.groovy
```

This will create a `.jar` in the `build` directory that we can run normally:

```java
java -jar build/service.jar
```

### The Docker Image

Lattice needs a reference to a Docker image, as deployed into a Docker image repository. There are private repositories available, but let's just use the publicly available Docker Hub.  If you haven't already created an account, [do so on the Docker Hub website](https://hub.docker.com/account/signup/).

We'll need to describe our application with a `Dockerfile`, as well:

```docker
FROM java
ADD ./build/service.jar /service.jar
ADD ./build/run.sh /run.sh
RUN chmod a+x /run.sh
CMD /run.sh
```

This file lives in the same directory as `service.groovy`. It tells Docker to use the `java` Docker image as the base. Docker images are made of layers, and - ideally - you should be able to reuse as much as possible of somebody else's layers. In this case, we're reusing [the standard `java` image](https://registry.hub.docker.com/_/java/). This image already has Linux and Java loaded in it.  We just need to derive from it and specialize it so that it knows about our application. The `ADD` directives _mount_ the resources from the local filesystem in the Docker image filesystem's root (`/`). The `RUN` directive tells Docker what we want done while building the image itself to customize the files that have been added. The `CMD` directive gives Docker a default command to run when the container starts up. In this case, it'll run `/run.sh`, which looks like this:

```bash
#!/bin/bash
java -jar /service.jar
```

With all of this in place, we can build and deploy a Docker image. To keep things simple, I use a Bash function to automatically build and deploy my Docker image:

```bash
function build_docker_image(){
  curdir=`dirname $0`
  target=$1
  app=$2
  user=$3
  cp $curdir/run.sh $target
  docker build -t $app .
  docker tag -f $app $user/$app
  docker push $user/$app
}
```

Then, I invoke the function inside of the same `.sh` script as follows:

```bash
build_docker_image build bootiful-docker starbuxman
```

I can now easily point Lattice to my deployed Docker image (`starbuxman/bootiful-docker`).

### Deploying to Lattice
