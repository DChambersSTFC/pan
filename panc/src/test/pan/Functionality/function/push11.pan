#
# @expect=org.quattor.pan.exceptions.EvaluationException
# @format=pan
#
object template push11;

'/x' = list(1,2,3);
'/x' = push(x[0],x[1],x[2],"4",null);