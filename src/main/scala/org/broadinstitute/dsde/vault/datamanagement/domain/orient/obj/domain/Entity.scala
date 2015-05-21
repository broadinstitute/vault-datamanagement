package org.broadinstitute.dsde.vault.datamanagement.domain.orient.obj.domain

import java.util.Date

import scala.beans.BeanProperty

class Entity {
  var guid: String = _
  var bossID: String = _
  var entityType: String = _
  var createdBy: String = _
  var createdDate: Date = _
  var modifiedBy: String = _
  var modifiedDate: Date = _
  @BeanProperty
  var attributes: java.util.List[Attribute] = new java.util.ArrayList[Attribute]
}
