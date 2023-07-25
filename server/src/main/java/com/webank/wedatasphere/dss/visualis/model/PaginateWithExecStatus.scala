package com.webank.wedatasphere.dss.visualis.model

import org.apache.linkis.scheduler.queue.SchedulerEventState
import edp.core.model.PaginateWithQueryColumns
import lombok.Data
import lombok.EqualsAndHashCode

@Data
@EqualsAndHashCode(callSuper = false) class PaginateWithExecStatus() extends PaginateWithQueryColumns {
  override def toString: = "PaginateWithExecStatus(execId=" + this.getExecId + ", status=" + this.getStatus + ", progress=" + this.getProgress + ")"

  override def equals(o: Any): = {
    if (o eq this) return true
    if (!o.isInstanceOf[PaginateWithExecStatus]) return false
    val other = o.asInstanceOf[PaginateWithExecStatus]
    if (!other.canEqual(this.asInstanceOf[Any])) return false
    val this$execId = this.getExecId
    val other$execId = other.getExecId
    if (if (this$execId == null) other$execId != null
    else !(this$execId == other$execId)) return false
    val this$status = this.getStatus
    val other$status = other.getStatus
    if (if (this$status == null) other$status != null
    else !(this$status == other$status)) return false
    if (java.lang.Float.compare(this.getProgress, other.getProgress) != 0) return false
    true
  }

  protected def canEqual(other: Any): = other.isInstanceOf[PaginateWithExecStatus]

  override def hashCode: = {
    val PRIME = 59
    var result = 1
    val $execId = this.getExecId
    result = result * PRIME + (if ($execId == null) 43
    else $execId.hashCode)
    val $status = this.getStatus
    result = result * PRIME + (if ($status == null) 43
    else $status.hashCode)
    result = result * PRIME + java.lang.Float.floatToIntBits(this.getProgress)
    result
  }

  private var execId = ""
  private var status = SchedulerEventState.Inited.toString
  private var progress = -1

  def getExecId = execId

  def setExecId(execId: String) = this.execId = execId

  def getStatus = status

  def setStatus(status: String) = this.status = status

  def getProgress = progress

  def setProgress(progress: Float) = this.progress = progress
}