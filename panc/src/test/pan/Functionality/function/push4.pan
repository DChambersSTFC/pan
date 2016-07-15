#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template push4;

'/y' = list(4,5,6);

'/x' = {
  y = value('/y');
  x = list(1,2,3);
  x = push(x[0],x[1],x[2],y[0],y[1],y[2]);
};

'/result' = {
  x = value('/x');
  (length(x) == 6 && x[2] == 3 && x[5] == 6);
};
