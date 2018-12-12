package edu.lu.uni.serval.alarm.tracking.entity
{
  sealed trait AlarmClass
  case object ClassAlarm extends AlarmClass
  case object MethodAlarm extends AlarmClass
  case object FieldAlarm extends AlarmClass
  case object ChunkAlarm extends AlarmClass
  case object RangelessAlarm extends AlarmClass // range = ("-1","-1")
  
  case object BlockAlarm extends AlarmClass // existing?

  case object Undefined extends AlarmClass
  //val daysOfWeek = Seq(Mon, Tue, Wed, Thu, Fri)
}