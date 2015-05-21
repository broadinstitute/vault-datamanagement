package org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.frames.{Adjacency, InVertex, OutVertex, EdgeFrame}

trait Relationship extends EntityBase {
  @Adjacency(label = "entity1", direction = Direction.IN)
  def getEntity1: Entity

  @Adjacency(label = "entity2", direction = Direction.OUT)
  def getEntity2: Entity

  @Adjacency(label = "entity1", direction = Direction.IN)
  def setEntity1(param: Entity): Unit

  @Adjacency(label = "entity2", direction = Direction.OUT)
  def setEntity2(param: Entity): Unit
}
