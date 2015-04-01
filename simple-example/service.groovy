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
