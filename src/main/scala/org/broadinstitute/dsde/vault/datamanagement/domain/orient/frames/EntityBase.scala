package org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames

import java.util.Date

import com.tinkerpop.frames.{Adjacency, Property}

trait EntityBase {
  @Property("guid")
  def getGuid: String

  @Property("bossID")
  def getBossID: String

  @Property("entityType")
  def getEntityType: String

  @Property("createdBy")
  def getCreatedBy: String

  @Property("createdDate")
  def getCreatedDate: Date

  @Property("modifiedBy")
  def getModifiedBy: String

  @Property("modifiedDate")
  def getModifiedDate: Date

  @Property("guid")
  def setGuid(param: String): Unit

  @Property("bossID")
  def setBossID(param: String): Unit

  @Property("entityType")
  def setEntityType(param: String): Unit

  @Property("createdBy")
  def setCreatedBy(param: String): Unit

  @Property("createdDate")
  def setCreatedDate(param: Date): Unit

  @Property("modifiedBy")
  def setModifiedBy(param: String): Unit

  @Property("modifiedDate")
  def setModifiedDate(param: Date): Unit

  @Adjacency(label = "hasAttribute")
  def addAttribute(attribute: Attribute): Unit

  @Adjacency(label = "hasAttribute")
  def getAttributes: java.lang.Iterable[Attribute]
}
