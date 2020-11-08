object ErrorHandler {

  def error(exception: Throwable): Unit = {
    println("ERROR: " + exception.toString)
  }

}
