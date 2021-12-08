// SPDX-License-Identifier: Apache-2.0

package chisel3

import chisel3.experimental.FixedPoint
import chisel3.stage.ChiselStage
import chisel3.util.Decoupled

object BundleComparator {
  def compare(bundle: Bundle): Boolean = {
    val header = s"=== Bundle Comparator ${bundle.className} " + "=" * 40
    println(header)
    println(f"${"New Field Name"}%30s ${"id"}%6s ${"Old Field Name"}%30s ${"id"}%6s")

    val newElements = bundle.elements.toList
    val oldElements = bundle.oldElementsNoChecks.toList

    var discrepancyFound = false
    newElements.zipAll(oldElements, "Oops" -> Bool(), "Oops" -> Bool()).foreach {
      case ((a, b), (c, d)) =>
        val color = if (a == c) { Console.RESET }
        else { discrepancyFound = true; Console.RED }
        println(f"$color$a%30s (${b._id}%06x) $c%30s (${d._id}%06x) ${Console.RESET}")
    }
    println("=" * header.length)
    discrepancyFound
  }
}

/* Demo stuff
 */
class BpipIsComplexBundle extends Module {

  trait BpipVarmint {
    val varmint = Bool()
    def vermin = Bool()
    private val puppy = Bool()
  }

  abstract class BpipAbstractBundle extends Bundle {
    def doNothing: Unit

    val fromAbstractBundle = UInt(22.W)
  }

  class BpipOneField extends Bundle {
    val fieldOne = SInt(8.W)
  }

  class BpipTwoField extends BpipOneField {
    val fieldTwo = SInt(8.W)
    val fieldThree = Vec(4, UInt(12.W))
  }
  class BpipAnimalBundle(w1: Int, w2: Int) extends Bundle {
    val dog = SInt(w1.W)
    val fox = UInt(w2.W)
  }

  class DemoBundle[T <: Data](gen: T, gen2: => T) extends BpipTwoField with BpipVarmint {
    val foo = gen
    val bar = Bool()
    val qux = gen2
    val bad = 44
    val baz = Decoupled(UInt(16.W))
    val animals = new BpipAnimalBundle(4, 8)
  }

  val out = IO(Output(new DemoBundle(UInt(4.W), FixedPoint(10.W, 4.BP))))

  val out2 = IO(Output(new BpipAbstractBundle {
    override def doNothing: Unit = println("ugh")
    val notAbstract:        Bool = Bool()
  }))

  val out4 = IO(Output(new BpipAnimalBundle(99, 100)))
  val out5 = IO(Output(new BpipTwoField))

  out := DontCare
  out5 := DontCare

  assert(!BundleComparator.compare(out5), "Bundle BpipTwoField not the same")
  assert(!BundleComparator.compare(out), "Bundle DemoBundle not the same")
  assert(!BundleComparator.compare(out2), "Bundle BpipAbstractBundle not the same")
  assert(!BundleComparator.compare(out4), "Bundle Animal not the same")

  println(s"Testing call ${out.elements.map(_._1).mkString(",")}")
  println(s"Testing call ${out.elements.map(_._1).mkString(",")}")
  println(s"Testing call ${out.elements.map(_._1).mkString(",")}")
  println(s"Testing call ${out.elements.map(_._1).mkString(",")}")
  println(s"Testing call ${out.elements.map(_._1).mkString(",")}")
  println(s"Testing call ${out.elements.map(_._1).mkString(",")}")
  println(s"Testing call ${out.elements.map(_._1).mkString(",")}")
}

/* Rich and complicated bundle example
 *
 */
object DebugProblem1 {
  def main(args: Array[String]): Unit = {
    ChiselStage.emitFirrtl(new BpipIsComplexBundle)
    println("done!")
  }
}

trait BpipSuperTraitWithField {
  val fromTraitOne = SInt(17.W)
  def notFieldOne = SInt(22.W)
}

trait BpipTraitWithField extends BpipSuperTraitWithField {
  val fromTraitTwo = SInt(17.W)
  def notFieldTwo = SInt(22.W)
}

class BpipOneField extends Bundle with BpipTraitWithField {
  val fieldZero = SInt(8.W)
  val fieldOne = SInt(8.W)
}

class BpipTwoField extends BpipOneField {
  val fieldTwo = SInt(8.W)
  val fieldThree = Vec(4, UInt(12.W))
  val myInt = 7
//  val baz = Decoupled(UInt(16.W))
}

class BpipDecoupled extends BpipOneField {
  val fieldTwo = SInt(8.W)
  val fieldThree = Vec(4, UInt(12.W))
  val baz = Decoupled(UInt(16.W))
}

class BpipBadBundleWithHardware extends Bundle {
  val noHardwareField = SInt(8.W)
  val isHardwareField = 244.U(16.W)
}

class BpipExtendsBadBundleWithHardware extends BpipBadBundleWithHardware {
  val anotherField = SInt(8.W)
}

class DebugProblem2 extends Module {
  val out1 = IO(Output(new BpipDecoupled))
  out1 := DontCare
  assert(!BundleComparator.compare(out1))
}

/* plugin should work with decoupled
 *
 */
object DebugProblem2 {
  def main(args: Array[String]): Unit = {
    ChiselStage.emitFirrtl(new DebugProblem2)
    println("done!")
  }
}

/* plugin should not affect the seq detection
 *
 */
class DebugProblem3 extends Module {
  val out1 = IO(Output(new BpipTwoField))
  out1 := DontCare
  assert(!BundleComparator.compare(out1))
}

object DebugProblem3 {
  def main(args: Array[String]): Unit = {
    ChiselStage.emitFirrtl(new DebugProblem3)
    println("done!")
  }
}

/* plugin should not affect the seq detection
 *
 */
class DebugProblem4 extends Module {
  val out1 = IO(Output(new BpipBadBundleWithHardware))
  out1 := DontCare
  println(s"out1.elements:\n" + out1.elements.map(e => s"${e._1} (${e._2})").mkString("\n"))
  println(s"out1.elements:\n" + out1.oldElementsNoChecks.map(e => s"${e._1} (${e._2})").mkString("\n"))
}

object DebugProblem4 {
  def main(args: Array[String]): Unit = {
    ChiselStage.emitFirrtl(new DebugProblem4)
    println("done!")
  }
}

/* plugin should not affect the seq detection
 *
 */
class DebugProblem5 extends Module {
  val out1 = IO(Output(new BpipExtendsBadBundleWithHardware))
  out1 := DontCare
  println(s"out1.elements:\n" + out1.elements.map(e => s"${e._1} (${e._2})").mkString("\n"))
}

object DebugProblem5 {
  def main(args: Array[String]): Unit = {
    ChiselStage.emitFirrtl(new DebugProblem5)
    println("done!")
  }
}

//TODO: If you comment out this block and compile, there will be a compiler
//      compiler error at the badSeqField
//class BpipBadSeqBundle extends Bundle {
//  val goodField = UInt(999.W)
//  val badSeqField = Seq(UInt(16.W), UInt(8.W), UInt(4.W))
//}
//
///* plugin should not affect the seq detection
// *
// */
//class DebugProblem6 extends Module {
//  val out1 = IO(Output(new BpipBadSeqBundle))
//  out1 := DontCare
//  println(s"out1.elements: \n" + out1.elements.map(e => s"${e._1} (${e._2})").mkString("\n"))
//}
//
//object DebugProblem6 {
//  def main(args: Array[String]): Unit = {
//    ChiselStage.emitFirrtl(new DebugProblem6)
//    println("done!")
//  }
//}

class BpipBadSeqBundleWithIgnore extends Bundle with IgnoreSeqInBundle {
  val goodFieldWithIgnore = UInt(999.W)
  val badSeqFieldWithIgnore = Seq(UInt(16.W), UInt(8.W), UInt(4.W))
}

/* plugin should not affect the seq detection
 *
 */
class DebugProblem7 extends Module {
  val out1 = IO(Output(new BpipBadSeqBundleWithIgnore))
  out1 := DontCare
  println(s"out1.elements: \n" + out1.elements.map(e => s"${e._1} (${e._2})").mkString("\n"))
}

object DebugProblem7 {
  def main(args: Array[String]): Unit = {
    ChiselStage.emitFirrtl(new DebugProblem7)
    println("done!")
  }
}

class BpipP8_1 extends Bundle {
  val field_1_1 = UInt(11.W)
  val field_1_2 = UInt(12.W)
}

class BpipP8_2 extends BpipP8_1 {
  val field_2_1 = UInt(11.W)
  val field_2_2 = UInt(12.W)
}

class BpipP8_3 extends BpipP8_2 {
  val field_3_1 = UInt(11.W)
  val field_3_2 = UInt(12.W)
}

/* plugin should not affect the seq detection
 *
 */
class DebugProblem8 extends Module {
  val out1 = IO(Output(new BpipP8_3))
  out1 := DontCare
  assert(!BundleComparator.compare(out1), "BpipP8_2 out of order")
}

object DebugProblem8 {
  def main(args: Array[String]): Unit = {
    ChiselStage.emitFirrtl(new DebugProblem8)
    println("done!")
  }
}

/* plugin should not affect the seq detection
 *
 */
class DebugProblem9 extends Module {
  // The following block does not work, suggesting that ParamIsField is not a case we need to solve
  class BpipParamIsField0(val paramField0: UInt) extends Bundle
  class BpipParamIsField1(val paramField1: UInt) extends BpipParamIsField0(UInt(66.W))

  val out3 = IO(Output(new BpipParamIsField1(UInt(10.W))))
  // println(s"ParamsIsField.elements:\n" + out3.elements.map(e => s"${e._1} (${e._2})").mkString("\n"))
  out3 := DontCare
  BundleComparator.compare(out3)
}

object DebugProblem9 {
  def main(args: Array[String]): Unit = {
    ChiselStage.emitFirrtl(new DebugProblem9)
    println("done!")
  }
}
