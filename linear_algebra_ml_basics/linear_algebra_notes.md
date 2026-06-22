## Table of contents 

- [1. Tensor](#Tensor)
- [2. Scalar](#Scalar)
  -  [2.1. google colab](#google-colab)
- [3. Vectors](#Vectors)
  - [3.1. Vector Transposition](#Vector-Transposition)
  - [3.2. Norms and unit vectors](#Norms-and-unit-vectors)
    - [3.2.1 Unit Vectors](#Unit-Vectors)
    - [3.2.2 L1 norm](#L1-norm)
    - [3.2.3 Squared L2 Norm](#Squared-L2-Norm)
    - [3.2.4 Max norm](#Max-norm)
    - [3.2.5 Generalized L<sup>p</sup> Norm](#Generalized-L-p-Norm)
  - [3.3 Basis, Orthogonal and Orthonormal vectors](#Basis--Orthogonal-and-Orthonormal-vectors)
    - [3.3.4 Orthogonal vectors](#Orthogonal-vectors)
- [4. Matrices](#Matrices)
  - [4.1 Generic Tensor notation](#Generic-Tensor-notation)
  - [4.2 Transpose matrix](#Transpose-matrix)
  - [4.3 Tensore Transpose](#Tensor-Transpose)
- [5. Basic tensor arithmetic](#Basic-tensor-arithmetic)
  - [5.1 Scalar operation](#Scalar-operation)
  - [5.2 Hadamard product](#Hadamard-product)
  - [5.3 Reduction](#Reduction)
  - [5.4 Dot prodcut](#Dot-product)
- [6 Solving Linear Systems](#Solving-Linear-Systems)
  - [6.1 Substitution](#Substitution)
  - [6.2 Elimination](#Elimination)
- [7. Matrix\-by\-vector Multiplication](#Matrix-by-vector-Multiplication)
- [8. Matrix\-by\-Matrix multiplication](#Matrix-by-Matrix-multiplication)
- [9. Symmetric and Identity Matrices](#Symmetric-and-Identity-Matrices)
  - [9.1 Symmetric Matrices](#Symmetric-Matrices)
  - [9.2 Identity Matrices](#Identity-Matrices)
- [10. ML and Deep Learning](#ML-and-Deep-Learning)  

## Tensor

  - This is the most common data structure used in Machine Learning and Deep Learning
  - Arrays of number
  - ML generalization of vectors and matrices to any number of dimensions

scalar x              - (0 dimensional tensor)
vector [x1 x2 x3]     - (1 dimensional tensor)
matrix                - (2 dimenstion has height and width )    
```
   | x1,1  x1,2 |
   | x2,1  x2,2 |
```
3 - tensor (higher dimensional tensor) 2 height, 2 width, 2 depth dimensional

```
        _ _
       /_/_/|
      /_/_/|/|
     |_|_|/|/
     |_|_|/
```

Generalized 

```
Dimensions      Math name          Description

0                scalar             magnitude only
1                vector             array
2                matrix             flat table, e.g. square
3                3-tensor           3D table, e.g. cube 
n                n-tensor           higher dimenstional
```

## Scalar
  - no dimension 
  - single number 
  - denotated in lowercase, italics, e.g x
  - should be typed, like all other tensors. eg. int, float32


## google colab
  - an environment which uses google account to run command
  
  https://github.com/the-deep-learners/TensorFlow-LiveLessons/blob/master/notebooks/deep_net_in_tensorflow.ipynb
  
  numpy
  pytourch
  tensorflow
  
## Vectors
  
   - 1-dimensional array of numbers
   - denoted in lowercase italics, bold, eg: x
   - Arranged in an order, so element can be accessed by its index
     - elemets are scarlars so not bold, eg. second element of x is x2
   - representing a point in space
     - Vector of length two represents location in 2D matrix    


   ```
       |                       [x1 x2] = [ 12 4]
   4   |---------------------|
       |                     |
  x2  |                     |
       |_____________________|___
   0,0  x1                 12
  ```

## Vector Transposition

```
              T       _  _
  [ x1 x2 x3 ]   =   | x1 |
                     | x2 |
                     | x3 |
                     |_  _|

row vector           = column vector 
 shape is (1, 3)            (3, 1)

``` 

## Norms and unit vectors

### Norms

```

     |        /                [x1 x2] = [ 12 4]
   4 |      /
     |    /
  x2 |  /                    
     |/________________________
	0,0  x1        12 
	
```

vector represents magnitude and direction from origin

Norms are functions that quantify vector magnitude.

The vector from origin to that point represented by the values is in this case (12,4) above 


The common Norm is L2 norm

 - simply the distance between the points 

- described as 

```
                          2    1/2
 || x ||    =  ( sum (  x   ) ) 
         2
```
		 
- square root of (sum of squares of all individual elements) 
- Measures simple distance (called Eucliden distance) from origin
- Most common norm in ML
  - instead of || x ||   , it can be denoted as || x || (without the base 2)
                       2

numpy
```
import numpy as np

x = np.array([25, 2, 5]) 

(25**2 + 2**2 + 5**2)**(1/2)
# 25.573423705088842

-- alternate option for norm
np.linalg.norm(x)
# np.float64(25.573423705088842)
```

### Unit Vectors

- special case of vector where its length is equal to one

- if || x || = 1, x is "unit vector"

```
      |      (1,1)
	1 |    /
	  |  /
	  |/__________
	(0,0)     1
	
- x is a unit vector with unit norm, if the L2 norm is 1.
```

### L1 norm

- described as

<img width="270" height="92" alt="image" src="https://github.com/user-attachments/assets/6b422a93-8cf1-4ea7-867a-0ec5a1d1aa95" />

```
      || x ||   = sum ( absolute value of x )
	          1
	 sum of all absolute values of each elements in vector
```

- Another common norm in ML
- varies linearly at all locations wheter near or far from origin
- used whenever difference between zero and non-zero is key

```
x = np.array([25, 2, 5])

np.abs(25) + np.abs(2) + np.abs(5)
32
```

### Squared L2 Norm

- described by 

<img width="220" height="146" alt="image" src="https://github.com/user-attachments/assets/ac2064f4-ea01-4c2e-ae19-836579da3e54" />

```
            2
     || x ||    = sum of ( square of all elements in vector) 
            2
```

Note, no square root


 - computiontally cheaper to use than L2 norm because:
    - squared L2 norm equsly simply x^T x  (i.e. x transpose x)
	  - derivative (used to train alone, whereas L2 norm requirees x vector)
 - Downside is it grows slowly near origin so can't be used if distinguishing between zero and near-zero is important
 
``` 
 (25**2 + 2**2 + 5**2)
 
 np.dot(x,x)
``` 
 
### Max norm

 Max Norm (or L<sup>infinity</sup> Norm)
 
- described

<img width="514" height="80" alt="image" src="https://github.com/user-attachments/assets/0b346ad9-c2b7-4ca3-beea-bc0e2f62b7de" />

```
|| x ||            =                   | x |
        infinity        max of element  

- max norm is max of absolute value of element in vector (simply the largest magnitude element)
```
                   
### Generalized L<sup>p</sup> Norm 

 - described by:
      
   <img width="380" height="106" alt="image" src="https://github.com/user-attachments/assets/a576db07-c408-4723-beee-b1266f952399" />

```	 
	    || x ||   =   (  sum of absolute elements power p ) power 1/p 
               p
			   
   - p must be 
	  - real number 
	  - Greader than or equal to one
   - Can derive L1, L2 and L infinity norm forumlae by substituting for p
   - Norms, particularly L1 and L2, used to regularize objective functions
```
   
### Basis, Orthogonal and Orthonormal vectors
   
   Basis vectors can be scaled to any vector

```
	   |
	   |
y    1 |                => v = 1.5i + 2j 
	 j |                     (1.5 times i + 2 times j)
	   |____________
	  0,0  i  1
	        x
```

### Orthogonal vectors

x and y are orthogonal vectors if  x<sup>T</sup> y = 0
 - perform x transpose y is zero then it is called orthogonal
 - are at 90 degree angle to each other ( assuming non-zero norms)
 - n-dimensional space has max n mutually orthogonal vectors (again, assuming non-zero norms)
 
 - Orthonormal vectors are orthogonal and all have unit norm; 
   - Basis vectors are an example 

```
        2 |    basis vectors  i(1,0) j(1,0)
    y     |
          |
        1 |
          |_______________
		        1    2
                   x 				
```

- example of orthonormal vector

```
i = np.array([1,0])
j = np.array([0,1])

np.dot(i,j)
# 0
```

### Matrices

 - Two dimensional array of numbers 
 - Denoted in uppercase, italics, bold, eg: X
 - Height given priority ahead of width in notation, i.e  n<sub>row</sub>, n<sub>col<sub>
    - rows is given priority then the column so row x column
    - below is a 3x2 matrics 
 - Individual scalar elements are denoted in uppercase, italics only 
    - element on top-right corner of martix below is X<sub>1,2</sub>

 - colon represents an entire row or column 
   - Left column of matrix X is X<sub>:,1</sub>
   - Middle row of matrix X is X<sub>2,:</sub>

 
```
    _          _            _     _
   | x1,1  x1,2 |          | 25  2 |
   | x2,1  x2,2 |          |  5 26 |
   | x3,1  x3,2 |          |  3  7 |
   | _         _|          |_     _|

```

- in the notebook we use double square bracket to represent the matrix

```
x1=np.array([[25,2],[5,26],[3,7]])

x1.shape  # 3,2

-- select row matrix 
x1[1,:]   # array([ 5, 26])

x1[:,0'  # array([25,  5,  3])
```

- tensorflow 

```
import tensorflow as tf

x_tf = tf.Variable([[25,2],[5,26],[3,7]])

tf.rank(x_tf) # <tf.Tensor: shape=(), dtype=int32, numpy=2>

tf.shape(x_tf) # <tf.Tensor: shape=(2,), dtype=int32, numpy=array([3, 2], dtype=int32)>
```

- pytourch

```
import torch 

x_pt = torch.tensor([[25, 2], [5,26], [3,7]])

x_pt.shape  # torch.Size([3, 2])

x_pt[1,:]   # tensor([ 5, 26])
```

### Generic Tensor notation

- upper case, bold, italics, sans serif, eg. X

- In a 4-tensor X, elements at position (i,j,k,l) denoated as x<sub>(i,j,kl)</sup>

Higher rank Tensors 
 - rank 4 tensors are used to hold image data, each dimension corresponds to 

  - number of images in training batch e.g. 32
  - image height in pixels e.g. 28 for MNIST digits
  - image width pixels, e.g 28
  - Number of color channels e.g. 3 for RGB


pytorch 

```
images_pt = torch.zeros([32, 28, 28, 3])

images_pt

tensor([[[[0., 0., 0.],
          [0., 0., 0.],
          [0., 0., 0.],
          ...,
          [0., 0., 0.],
          [0., 0., 0.],
          [0., 0., 0.]],
		  ....
```  

tensor flow 

```
img_tf = tf.zeros([32,28,28,3])
```

### Transpose matrix

numpy 

```
in_np = np.array([[25, 2, -3, -23]])

np.shape(in_np)  # (1,4)

in_npT = in_np.T

np.shape(in_npT)  # (4,1)

in_np = np.array([[1, 2, 3],[2,4,6],[4,8,24]]) 

in_np.shape  # 3,3

in_np

## output
array([[ 1,  2,  3],
       [ 2,  4,  6],
       [ 4,  8, 24]])
	   
in_npT = in_np.T

in_npT

## output
array([[ 1,  2,  4],
       [ 2,  4,  8],
       [ 3,  6, 24]])
```

### Tensor Transpose 

<img width="256" height="176" alt="image" src="https://github.com/user-attachments/assets/c13db99f-c724-43eb-96b7-3cb64ec31df0" />

<img width="314" height="138" alt="image" src="https://github.com/user-attachments/assets/d1b3f126-f991-44f6-ad5b-743701f238e7" />

- Transpose of scalar is itself, e.g  x<sup>T</sup> = x
- transpose of vector, converts column to row (vice versa)
- scalar and vector transposition are special cases of matrix transposition
   - flip of axes over main diagonal such that
  
   - <img width="374" height="144" alt="image" src="https://github.com/user-attachments/assets/bed2c9a6-fc4a-4bed-a24d-0fed364b587d" />

   
```
   _           _ T        _                   _
  |  x1,1  x1,2 |        |   x1,1  x2,1  x3,1  |
  |  x2,1  x2,2 |   =    |   x1,2  x2,2  x3,2  |            
  |  x3,1  x3,2 |        |_                   _|
  |_           _|        

```

- pytorch

```
import torch
x_pt = torch.tensor([[25, 2], [5,26], [3,7]])
x_pt.shape

x_pt.T
```

- tensorflow

```
tf_i = tf.Variable([[25, 2,5], [5,26,1]])
tf.transpose(tf_i)

## output
<tf.Tensor: shape=(3, 2), dtype=int32, numpy=
array([[25,  5],
       [ 2, 26],
       [ 5,  1]], dtype=int32)>
```

### Basic tensor arithmetic

#### Scalar operation

```
x=np.array([[1,2,4]])

x * 2

x *2+2

# torch representation

torch.add(torch.mul(x_pt,2),2)

# tensor flow 
x_pt * 2 + 2 
# or 
tf.add(tf.multiply(x_tf, 2),2)
```

#### Hadamard product 

- If two tensors have the same size, operations are often by default applied element-wise.
This is not matrix multiplcation, rather called hadamard product or simply element-wise product

Mathmetically represented as A (.) X

```
X=np.array([[ 1,  2,  3],
       [ 2,  4,  6],
       [ 4,  8, 24]])
	   
A= X+2
A
##
array([[ 3,  4,  5],
       [ 4,  6,  8],
       [ 6, 10, 26]])

A+X

## 
array([[ 4,  6,  8],
       [ 6, 10, 14],
       [10, 18, 50]])


A * X

##
array([[  3,   8,  15],
       [  8,  24,  48],
       [ 24,  80, 624]])

# pytorch and tensorflow the above operation is same 
```

### Reduction

- Caculating sum accross all elements of a tensor is common operation
- for vector x of length n, we calcuate sum from i=1 to n of x<sub>i</sub>
- for matrix X with m by n dimensions, we calcuate sum from i=1 to m sum from i=1 to n X<sub>i,j</sub>

- numpy
  
```
X
## output
array([[ 1,  2,  3],
       [ 2,  4,  6],
       [ 4,  8, 24]])

X.sum()
np.int64(54)

# torch 
torch.sum(x_pt)

# tensorflow
tf.reduce_sum(x_tf)
```
to sum along the axis

```
X.sum(axis=0)

## summ of all colums
array([ 7, 14, 33])

torch.sum(x_pt,0)

tf.reduce_sum(x_tf,1)
```

### Dot product 

- if we have two vector x and y, with the same length n
- we can calcualte the dot product between them
- annotated as x . y, x<sup>T</sup> y, <x , y>
- Regardless of notation, the calculation is the same

```
x.y = sum from i=1 to n of x<sub>i</sub> y<sub>j</sub>
```

- Dot product is ubiquitios in deep learning. It is perfromed on every artificial neuron in a deep neural network, which may be made up to millions of these neurons

```
x = np.array([25,2,5])
y= np.array([0,1,2])
np.dot(x,y)  # np.int64(12)

# torch
x_pt
y_pt = torch.tensor([0,1,2])

# for pytorch pass in float time for operations else it will throw error
torch.dot(torch.tensor([25,2,5.]),torch.tensor([0,1,2.]))


# tensor flow 
x_tf

y_tf = tf.Variable([0,1,2])

# no dot function in tensorflow so we need to use the formula

tf.reduce_sum(tf.multiply(x_tf, y_tf))

```

## Solving Linear Systems 

### Substitution 

- subsititution is a method to solve linear systems

When to use?
 - whenever there's a variable in system with coefficient of 1
 - example, when solving for x and y in following system
   y = 3x 
   -5x + 2y = 2
 - in the above we can substitue y in the above equation
 
ex.
 x+y =6 and 2x+3y = 16
 -x+4y=0 and 2x - 5y = -6
 y=4x + 1 and -4x + y =2
 
### Elimination 

When to use?
 - typically best option if no variable in system has coefficient 1
 - Use addition property of equations to elimnate variables
   - If necessary, multiply one or both equations to make elimination of a variable possible
  
Example:
  2x - 3y = 15 
  4x + 10y = 14
 
 multiply by -2 on the 1st equation, so we can elimnate the x on both equation when added 

## Matrix-by-vector Multiplication

Conditions:
  Two matrix can be multiplied only when the 

   A matrix with a-row, a-column 
   B matrix with b-row, b-column 
   we can multiply the A and B only when 
     - A a-column = B b-row 
     - the result would be of a-row, b-column 

```

   C      =      A       *       B
     m,p           m,n             n,p
	 

   C       =  sum for each j of product/multiplictaion of A     .  B 
     i,k                                                    i,j      j,k

   i,j and j,k are row,column represenation of A and B 
```	 
 
Example:

Multiply matrix with vector like below where 1 dimension vector

```
   _      _      _    _
  |  3  4  |    |   1  |
  |  5  6  |    |   2  |
  |  7  8  |    |_    _|
  |_      _|  

  3,2              2,1  
  
  
        _          _
  =    | 3x1 + 4x2  |
       | 5x1 + 6x2  |
       | 7x1 + 8x2 	|   
       |_          _|
	   
  =     _    _
       |  11  |
	   |  17  |
	   |_ 23 _|
```

colab or notbook

```
A = np.array([[3,4],[5,6],[7,8]])

b = np.array([1,2])

np.dot(A, b)

# torch 

A_pt = torch.tensor([[3,4],[5,6],[7,8]])

b_pt = torch.tensor([1,2])

torch.matmul(A_pt, b_pt)

# tensor flow 

A_tf = tf.Variable([[3,4],[5,6],[7,8]])

b_tf = tf.Variable([1,2])

tf.linalg.matvec(A_tf, b_tf)
```

## Matrix-by-Matrix multiplication

- The same rule applies the column of first matrix should be same as the row of the second matrix

```

   _      _      _       _
  |  3  4  |    |   1  9  |
  |  5  6  |    |   2  0  |
  |  7  8  |    |_       _|
  |_      _|  

  3,2            2,2
  
  
        _                       _
    =  | 3x1 + 4x2    3x9 + 4x0  |
       | 5x1 + 6x2    5x9 + 6x0  |
       | 7x1 + 8x2 	  7x9 + 8x0  |   
       |_                       _|

    
	=   _         _
       |  11  27   |
	   |  17  45   |
	   |_ 23  63  _|
```

colab or notebook

 - Matrix multiplication is not commutative (i.e. A.B not equat to B.A)

```

# numpy

A = np.array([[3,4],[5,6],[7,8]])

B = np.array([[1,9], [2,0]])

np.dot(A, B)


# torch

A_pt = torch.from_numpy(A)
B_pt = torch.from_numpy(B)

# alternate to create B tensor with transposition

B_pt = torch.tensor([[1,2], [9,0]]).T

# matrix multiplication
torch.matmul(A_pt,B_pt)

# tensor flow 

# convert nparray with tensorflow 
A_tf = tf.convert_to_tensor(A, dtype=tf.int32)
B_tf = tf.convert_to_tensor(B, dtype=tf.int32)

tf.matmul(A_tf, B_tf)
```

## Symmetric and Identity Matrices

### Symmetric Matrices

- Special matrix and includes below properties 
   
  - square matrics, rows and colums are equal.
  - X<sup>T</sup> = X   ( transpose of x = x)  
   
```
   _	      _
  |   0  1   2 |
  |   1  7   8 |
  |   2  8   9 |
  |_          _|
```

colab 

```
# numpy

x_sym = np.array([[0,1,2],[1,7,8],[2,8,9]])

x_sym.T


x_sym.T == x_sym
```

### Identity matrices

A symmetric matrix where 
 - Every element along main diagonal is 1 
 - All other elements are 0
 - Notation: I<sub>n</sub> where n= height (or width)
 - n-length vector unchanged if multipled by I<sub>n</sub> 

```
  _             _
 |   1  0  0  0  |
 |   0  1  0  0  |
 |   0  0  1  0  |
 |_  0  0  0  1 _|
 
```

colab 

```
I = torch.tensor([[1,0,0],[0,1,0],[0,0,1]])

x_pt = torch.tensor([25,2,5])

toruch.matmul(I, x_pt)
```

 - exercise

```
import numpy as np

A = np.array([[0,1,2],[3,4,5],[6,7,8]])
B = np.array([-1,1,-2])

np.dot(A,B)

# output - array([ -3,  -9, -15])

A1 = np.array([[0,1,2],[3,4,5],[6,7,8]])
B1 = np.array([[-1,0],[1,1],[-2,2]])

np.dot(A1,B1)

# output
array([[ -3,   5],
       [ -9,  14],
       [-15,  23]])

```

## ML and Deep Learning 


y = a + f x<sub>1</sub> + c x<sub>2</sub> + ..... + m x<sub>m</sub> 


```
  _                                                _
 |    y1  |   a + b x1,1 + c x1,2 + .... + m x1,m   |
 |    y2  |   a + b x2,1 + c x2,2 + .... + m x2,m   |
 |     .  |   .                                     |
 |     .  |   .                                     |
 |    yn  |   a + b xn,1 + c xn,2 + .... + m xn,m   | 
 |_                                                _|

Dataset:
  For any house i in the dataset,
  yi = price and xi,1 to xi,m are its features
  we solve for parameters a,b,c to m
  x extends rightward to m-1 not m because of the presence of a on far left 
```

The above equation can be represented in matrix 


Matrix Multiplication in regression

```
   _     _         _                         _     _    _
  |   y1  |       |   1  x1,1  x1,2 ... x1,m  |   |  a   |
  |   y2  |       |   1  x2,1  x2,2 ... x2,m  |   |  b   |
  |   .   |   =   |   .  .      .   ...       |   |  c   |
  |   .   |       |   .  .      .   ...       |   |  .   |
  |   yn  |       |   1  xn,1  xn,2 ... xn,m  |   |_ m  _|
  |_     _|       |_                         _| 

    m - features wide
```

For example refer the artificial-neurons.ipynb file in (git Repo)[https://github.com/jonkrohn/ML-foundations/blob/master/notebooks/artificial-neurons.ipynb] - https://github.com/jonkrohn/ML-foundations

For deep learning - https://github.com/jonkrohn/deepTF1

## Frobenius Norm

- Analogus to L<sup>2</sup> Norm
- Measures the size of matrix in terms of Euclidean distance
- It is the sum of the magnitude of all the vectors (that makes the columns on the matrix) in X

Described as:
 <img width="682" height="242" alt="image" src="https://github.com/user-attachments/assets/4f0363bc-1ba9-4244-bad2-ba782e65bfef" />

Frobenius Norm is matrix norm of an m x n matrix A defined as square root of the sum of the absolute squares of its elements. This can also be considered as Vector norm.

<img width="346" height="144" alt="image" src="https://github.com/user-attachments/assets/1d6ac4e2-9e7a-4685-9074-61a153f304a8" />

- colab

```
import numpy as np

x = np.array([[1,2],[3,4]])

# calcuate the frobenius norm 
np.linalg.norm(x)

# output
np.float64(5.477225575051661)

# pytorch

x_pt = torch.tensor([[1,2],[3,4.]])

torch.norm(x_pt)

# output
tensor(5.4772)

# tensorflow
x_tf = tf.Variable([[1,2],[3,4.]])

tf.norm(x_tf)

# output
<tf.Tensor: shape=(), dtype=float32, numpy=5.4772257804870605> 

```

