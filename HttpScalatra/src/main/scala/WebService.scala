import org.scalatra.ScalatraServlet
import java.sql.{Connection, DriverManager, ResultSet}

import net.liftweb.json._

class WebService extends ScalatraServlet {
  get("/") {
    "Ez"
  }
  post("/handleJson") {
    val jsonString = request.body
    //implicit val formats = DefaultFormats
    val jValue = parse(jsonString)
    //val stock = jValue.extract[Stock]

    val con_st = "jdbc:postgresql://localhost:5432/task?user=postgres"
    val conn = DriverManager.getConnection(con_st)
    try {
      val prep = conn.prepareStatement("INSERT INTO task (status, req_body) VALUES (?, ?) ")
      prep.setInt(1, 0)
      prep.setString(2, compactRender(jValue))
      prep.executeUpdate()
      println("Success!")
    }
    catch {
      case e: Exception => {
        val prep = conn.prepareStatement("INSERT INTO task (status, err_msg) VALUES (?, ?) ")
        prep.setInt(1, 2)
        prep.setString(2, e.toString)

      }
    }

    finally {conn.close()}
    //println(stock)
  }
}

class Stock (var symbol: String, var price: Double) {
  override def toString = symbol + ", " + price
}


/*
CREATE TABLE task (
id SERIAL PRIMARY KEY,
date TIMESTAMP DEFAULT NOW(),
status INT NOT NULL,
err_msg VARCHAR ( 50 ) DEFAULT NULL,
req_body VARCHAR ( 255 ) DEFAULT NULL
)
 */