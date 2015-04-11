# Lattice, a Distributed Runtime for your Containerized Spring Workloads

## Portable Applications

Spring has always tried to make application portability as easy as possible. It supports good design patterns such as those prescribed by the [12 Factor App](http:/12factor.net) manifesto. We've even covered some of them, like  [backing services](https://spring.io/blog/2015/01/27/12-factor-app-style-backing-services-with-spring-and-cloud-foundry) and [externalized configuration](https://spring.io/blog/2015/01/13/configuring-it-all-out-or-12-factor-app-style-configuration-with-spring), in this very space recently.

As developers, we're used to being able to test applications in isolation, to validate the inputs into an application and validate the resulting behavior.  We're used to reproducible builds; we're used to being able to throw away the build and - with the disciplined use of tags and so on - reproduce the same build. This isn't news.

## Cattle

It took us developers a while to get to this place, but we did. In the last 8 years we've seen this rigor come to operations teams. Operations want reproducible infrastructure as much as we developers want reproducible builds. Consistency and reproducibility are even more important with disposable, ephemeral, cloud infrastructure. As Adrian Cockcroft (formerly at Netflix) reminds us: treat servers like cattle, not pets.

It's 2015, things have come a long way. Developers and operations are - ideally at least - two highly integrated teams. Developers want consistency in the way their applications run, operations want consistency in their infrastructure.  

## Deploy Containers

[Linux Containers, or _LXC_](http://wikipedia.com/wiki/LXC), are a set of features in the Linux kernel designed to isolate applications. Applications run as though they've got the run of the kernel. LXC aren't, then, a single API or feature, but a set of independant ones that can be used together.  Container technologies like Docker and Rocket leverage LXC features and let you control them declaratively. Using Docker, for example, you can write a Dockerfile, check it into your code and then use that Docker file and code to reproduce both the application and its environment, and do so at a much lower runtime footprint than traditional virtualization. This makes things simpler: operations deploy containers, not applications. To learn more about [using Spring Boot and Docker, check out this handy guide](https://spring.io/guides/gs/spring-boot-docker/).

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
  target=$curdir/$1
  app=$3
  user=$2
  cp $curdir/run.sh $target
  docker build -t $app $curdir
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
Make sure that [Lattice is up and running](http://lattice.cf/docs/getting-started/) and that you have the `ltc` CLI installed. The following Bash function will deploy your container to Lattice, make sure there are 5 instances of the application running, and then show informaion related to what applications are runninng (a bit like `ps aux`) and then show specific state for our application.

```bash
function deploy_to_lattice(){
  app=$2
  user=$1

  ltc rm $APP
  ltc create $APP $user/$APP -- /run.sh
  ltc scale $app 2

  ltc list
  ltc status $app
}
```

This function removes the existing app, if it's available, creates a new one (in this case, named `bootiful-docker`), then scales that application to have 5 concurrent running instances. Finally, `ltc list` is sort of like `ps aux` for Lattice - it'll display all the running processes. `ltc status` gives specific information about our deployed application.

Use it as follows:

```bash
deploy_to_lattice starbuxman bootiful-docker
```

You'll see output on the shell confirming that the application has been run, like this:

```bash
~/D/b/simple-example git:experiment ❯❯❯ ltc list                                                                                                     ✱
App Name			Instances	DiskMB		MemoryMB	Route
bootiful-docker			2/2		1024		128		bootiful-docker.192.168.11.11.xip.io, bootiful-docker-8080.192.168.11.11.xip.io => 8080
..
```

Visit `http://bootiful-docker.192.168.11.11.xip.io/hello/Lattice` to see the output of the REST endpoint. Visit `http://bootiful-docker.192.168.11.11.xip.io/env` (which comes from Spring Boot's Actuator module) to see the enumeration of the environment in which the application's running. Finally, visit http://bootiful-docker.192.168.11.11.xip.io/killme`
 to kill an instance. This will cause an instance of the application to exit. Lattice will immediately restart the instance. If you cause an instance of lattice-app to exit repeatedly Lattice will eventually start applying a backoff policy and restart the instance only after increasing intervals of time (30s, 60s, etc..).

The application has access to interesting environment variables like `CF_INSTANCE_IP`, `CF_INSTANCE_PORT`, which tell the running application the IP address and port used to address the containerized application from the outside. To learn more about this, [check out the docs on Lattice's environment](https://github.com/cloudfoundry-incubator/receptor/blob/master/doc/environment.md)

### Deploying and Consuming a Backing Service with Lattice

Thus far we've just deployed an HTTP service. Lattice handily supports all manner of containerized workloads. It's worth noting that the routes don't work for non HTTP traffic. Only TCP. If you want to to talk to a node, you'll need to use its IP address, directly. You can retreive the IP using the aforementioned environment variables, or just use `ltc status $APP_NAME`.


Let's standup PostgreSQL. There are any number of readily available, containerized infrastructure available for the taking on Docker Hub. Just find one and then deploy it to Lattice, like this:

```bash
ltc create --run-as-root bootiful-docker-postgres postgres
```
This will launch the [PostgreSQL Docker image](https://registry.hub.docker.com/_/postgres/) from Docker Hub. You can customize the running PostgreSQL instance by passing in environment variables, like this:

```bash
ltc create --run-as-root --env "POSTGRES_PASSWORD=pw" bds postgres
```

On my machine, `ltc status` yeilded the following:

```bash
~ ❯❯❯ ltc status bootiful-docker-postgres
================================================================================
      bootiful-docker-postgres
--------------------------------------------------------------------------------
Instances	1/1
Stack		lucid64
Start Timeout	0
DiskMB		1024
MemoryMB	128
CPUWeight	100
Ports		  5432
Routes		bootiful-docker-postgres.192.168.11.11.xip.io => 5432
		      bootiful-docker-postgres-5432.192.168.11.11.xip.io => 5432
--------------------------------------------------------------------------------
Environment

POSTGRES_PASSWORD="pw"
PORT="5432"

================================================================================
      Instance 0  [RUNNING]
--------------------------------------------------------------------------------
InstanceGuid	5b6e33a5-79f1-4311-4a2c-ba7108063fb1
Cell ID		lattice-cell-01
Ip		192.168.11.11
Port Mapping	61002:5432
Since		2015-04-03 10:57:48 (PDT)
Crash Count 	0
--------------------------------------------------------------------------------

```

I could then access the PostgreSQL instance like this:
```bash
psql -U postgres -h 192.168.11.11 -p 61002 postgres
```
