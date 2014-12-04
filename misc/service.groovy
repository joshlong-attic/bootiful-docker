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
}
