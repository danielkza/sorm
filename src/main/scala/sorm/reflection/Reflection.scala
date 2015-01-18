package sorm.reflection

import ref.WeakReference
import reflect.runtime.universe._

import sext._
import util.hashing.MurmurHash3
import com.typesafe.scalalogging.slf4j.LazyLogging

import ScalaApi._

class Reflection (
    protected[Reflection] val t : Type,
    loader : ClassLoader
) {
  protected def sym : Symbol = t.s

  private val classLoaderRef = WeakReference(loader)
  def classLoader = classLoaderRef.get.get
  private def mirror = runtimeMirror(classLoader)

  override def toString = {
    def typeName(t : Type) : String = t match {
      case tr: TypeRef if tr.args.nonEmpty =>
        val generics = tr.args.map(typeName).mkString(",")
        s"$tr[$generics]"
      case tt => tt.toString
    }
    s"Reflection[${typeName(t)}](${classLoader.toString})"
  }

  override def hashCode
    = MurmurHash3.finalizeHash(t.typeSymbol.hashCode, generics.hashCode())

  override def equals ( other : Any )
    = other match {
        case other : Reflection =>
          t =:= other.t
        case _ =>
          false
      }

  def <:< ( other : Reflection ) = t <:< other.t
  def =:= ( other : Reflection ) = t =:= other.t

  def properties
    = t.properties.view
        .map{ s => s.decodedName -> Reflection(s.t, classLoader) }
        .toMap
  def generics
    = t match {
        case t : TypeRef => t.args.view.map{ Reflection(_, classLoader) }.toIndexedSeq
        case _ => Vector()
      }
  def name
    = sym.decodedName
  def fullName
    = sym.ancestors.foldRight(""){ (s, text) =>
        if( text == "" ) s.decodedName
        else if( s.owner.isClass ) text + "#" + s.decodedName
        else text + "." + s.decodedName
      }
  def signature : String
    = t.toString

  def instantiate
    ( params : Map[String, Any] )
    : Any
    = t.constructors
        .view
        .zipBy{ _.paramss.view.flatten.map{_.decodedName} }
        .find{ _._2.toSet == params.keySet }
        .map{ case (c, ps) => sym.instantiate( c, ps.map{params} ) }
        .get

  def instantiate
    ( params : Seq[Any] )
    : Any
    = sym.instantiate(t.constructors.head, params)

  def propertyValue
    ( name : String,
      instance : AnyRef )
    : Any
    = instance.getClass.getMethods.find(_.getName == name).get.invoke(instance)

  def propertyValues
    ( instance : AnyRef )
    : Map[String, Any]
    = properties.keys.view.zipBy{ propertyValue(_, instance) }.toMap

  def primaryConstructorArguments
    : Seq[(String, Reflection)]
    = t.constructors.head.paramss.flatten.view
        .map{ s => s.decodedName -> Reflection(s.t, classLoader) }

  /**
   * Either the type itself if it's not mixed in or the first of its parents
   */
  def mixinBasis
    = t match {
        case t : RefinedType => Reflection(t.parents.head, classLoader)
        case _ => this
      }

  def containerObjectName : Option[String]
    = t.trying(_.asInstanceOf[TypeRef]).map(_.pre.s.decodedName)

  def containerObject : Option[Any]
    = t match {
        case t : TypeRef =>
          t.pre.typeSymbol match {
            case s =>
              Some(
                mirror.reflectModule(
                  s.owner.typeSignature.member(s.name.toTermName).asModule
                ).instance
              )
          }
        case _ => None
      }

  def isCaseClass
    = sym match {
        case s : ClassSymbol => s.isCaseClass
        case _ => false
      }

  def javaClass = mirror.runtimeClass(t)

  def withLoader(loader: ClassLoader) = Reflection(t, loader)
}

object Reflection extends LazyLogging {
  def apply( t : Type, l : ClassLoader) : Reflection =
    new Reflection(t, l)
  def apply[ A : TypeTag ](l : ClassLoader) : Reflection =
    Reflection(typeOf[A], l)
  def apply( t : Type) : Reflection =
    Reflection(t, Thread.currentThread().getContextClassLoader)
  def apply[ A : TypeTag ] : Reflection =
    Reflection(typeOf[A])
}
