package org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames

import com.tinkerpop.frames.Property

trait Attribute {
  @Property("name")
  def getName: String

  @Property("value")
  def getValue: String

  @Property("name")
  def setName(param: String): Unit

  @Property("value")
  def setValue(param: String): Unit
}
