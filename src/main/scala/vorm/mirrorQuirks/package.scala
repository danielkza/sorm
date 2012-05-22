package vorm

import reflect.mirror._

/**
 * Trickery and black magic around Scala's Mirror API
 */
package object mirrorQuirks {

  def classByName(n: String) =
    symbolToClass(symbolForName(n))

  def name(s: Symbol) =
    s.name.decoded.trim match {
      //  mixed in types bug workaround
      case "<refinement>" =>
        s.typeSignature.parents.head.typeSymbol.name.decoded
      case n => n
    }


  def javaClassName(s: Symbol): String =
    s.owner match {
      case o if o.isPackageClass =>
        s.fullName
      case o if o.isClass =>
        javaClassName(o) + "$" + name(s)
    }

  def symbolsTree(s: Symbol): List[Symbol] =
    s.owner match {
      case NoSymbol => Nil
      case o => s :: symbolsTree(o)
    }

  def isObject(s: Symbol) =
    s.kind == "object"

  def isClass(s: Symbol) =
    s.kind == "class"

  /**
   * Checks whether this symbol is nested inside a class (not a static object)
   */
  def isInner(s: Symbol) =
    isClass(s.owner)

  def fullName(s: Symbol) =
    symbolsTree(s).foldRight("") {
      (s, text) =>
        if (text == "") name(s)
        else if (isInner(s)) text + "#" + name(s)
        else text + "." + name(s)
    }
}
