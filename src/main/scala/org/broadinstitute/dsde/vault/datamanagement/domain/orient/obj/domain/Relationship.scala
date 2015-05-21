package org.broadinstitute.dsde.vault.datamanagement.domain.orient.obj.domain

import scala.beans.BeanProperty

class Relationship extends Entity {
  var entity1: Entity = _
  var entity2: Entity = _
}
