package org.broadinstitute.dsde.vault.datamanagement.domain.orient.frames

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.frames.Adjacency
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy

trait Entity extends EntityBase {
  @Adjacency(label = "entity1", direction = Direction.OUT)
  def addChildRelationship(param: Relationship): Unit

  @Adjacency(label = "entity2", direction = Direction.IN)
  def addParentRelationship(param: Relationship): Unit

  @Adjacency(label = "entity1", direction = Direction.OUT)
  def getChildRelationships: java.lang.Iterable[Relationship]

  @Adjacency(label = "entity2", direction = Direction.IN)
  def getParentRelationships: java.lang.Iterable[Relationship]
}
