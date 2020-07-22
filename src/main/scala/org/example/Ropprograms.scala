package org.example


import cats.effect._
import cats.implicits._
import monix.eval._
import monix.execution.Scheduler.Implicits.global
import monix.reactive._


object Ropprograms extends TaskApp {

  def parallel_task(i: Int) : Unit = {
    val genItems : Int => Array[Int] = n => (0 until n * 1024 * 1024).map(i => 1).toArray
    
    val tasks =  (0 until i).map(i => Task(genItems(2)).map(i => i.length))
    val window = tasks.sliding(4,4)
    val batches = window.map(batch => Task.parSequence(batch)).toIterable
    Task.parSequence(batches).foreach(b => b.foreach(arr => println(arr)))
    //.map(_.flatten.toList).foreach(l => println(l))
  }


  /** App's main entry point. */
  def run(args: List[String]): Task[ExitCode] = {

    //Hello.parallel_task(128)
    val list: Seq[Task[Int]] = Seq.tabulate(100)(i => Task(i))
    val chunks = list.sliding(10, 10).toSeq

    // Specify that each batch should process stuff in parallel
    val batchedTasks = chunks.map(chunk => Task.parSequence(chunk))

    // Sequence the batches
    val allBatches = Task.sequence(batchedTasks)
    val total = allBatches.map(_.flatten.sum)
    def printer(e : Either[Throwable, Int]) : Unit = 
      e match {
        case Left(t) => println("Exception was thrown")
        case Right(i) => println("The value was " + i.toString())
      }
    

    val r = total.runAsync(printer)

    args.headOption match {
      case Some(name) =>
        Task(println(s"Hello, $name!")).as(ExitCode.Success)
      case None =>
        Task(System.err.println("Usage: Hello name")).as(ExitCode(2))
    }
  }
}