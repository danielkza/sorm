package sorm

/**
 * ==Persisted trait and ids==
 * 
 * All the entities returned from SORM have a `Persisted` trait with an appropriate value of `id` mixed in. This is what lets SORM decide whether to `INSERT` rows or `UPDATE` them (and which ones) when the save operation is called. This also provides you with access to its generated `id`.
 * Since the `id` property value is meant to be generated by database, it is protected from the user of being able to manually specify it as well as letting the case classes have such a property.
 * So, instead of
 * {{{
 * case class Artist ( id : Long, name : String )
 * }}}
 * you should use
 * {{{
 * case class Artist ( name : String )
 * }}}
 * and let SORM take care of id property management for you.
 * 
 * ==What should you do when you need to get an id of an entity?==
 * 
 * Just do
 * {{{
 * artist.id
 * }}}
 * but for you to be able to do that the artist value must have a Persisted trait mixed in (i.e., have a type Artist with Persisted), which can happen only in three cases:
 * When you store a value in the db:
 * {{{
 * val artist = Db.save(Artist("Metallica"))
 * }}}
 * When you fetch it from the db:
 * {{{
 * val artist = Db.query[Artist].whereEqual("name", "Metallica").fetchOne().get
 * }}}
 * When you make a copy of an already persisted entity:
 * {{{
 * val artist = someOtherPersistedArtist.copy(name = "METALLICA")
 * }}}
 */
trait Persisted {
  def id : Long
  def demixinPersisted[ T ] : T
}
