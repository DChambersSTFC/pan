#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template push1;

'/x' = {
  x[0] = 1;
  x[1] = 2;
  x[2] = 3;
  x = push(x[0],x[1],x[2],4,5,6);
};

'/result' = {
  x = value('/x');
  (length(x) == 6 && x[3] == 4);
};
