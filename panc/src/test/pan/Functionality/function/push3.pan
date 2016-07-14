#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template push3;

'/x' = {
  x[0] = 1;
  x[1] = 2;
  x[2] = 3;
  x = push(x[0],x[1],x[2],1,2,3);
};

'/result' = {
  x = value('/x');
  (length(x) == 6 && x[2] == 3 && x[5] == 3);
};
