#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template append1;

'/x' = {
  x[0] = 1;
  x = push(2);
};

'/result' = {
  x = value('/x');
  (length(x) == 2 && x[1] == 2);
};
