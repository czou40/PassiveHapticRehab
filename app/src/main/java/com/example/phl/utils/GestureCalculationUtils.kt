package com.example.phl.utils
import org.apache.commons.math3.linear.EigenDecomposition
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.SingularValueDecomposition
import kotlin.math.pow
import kotlin.math.sqrt

class GestureCalculationUtils {
    companion object {

        val PALM_POINTS = listOf(0, 5, 17)

        val targetGesture3dData = arrayOf(
                doubleArrayOf(0.07425961, -0.02521384, -0.0193575),
                doubleArrayOf(0.06077141, 0.00829636, -0.02610136),
                doubleArrayOf(0.036636, 0.03090633, -0.01649983),
                doubleArrayOf(0.01144599, 0.04830192, -0.01380348),
                doubleArrayOf(-0.01001847, 0.03878592, -0.00856504),
                doubleArrayOf(0.00664921, 0.0280657, 0.00570075),
                doubleArrayOf(-0.0015232, 0.03520311, -0.01621959),
                doubleArrayOf(0.00527204, 0.03188517, -0.02243766),
                doubleArrayOf(0.01709405, 0.0281655, -0.00969717),
                doubleArrayOf(-0.00247982, 0.00614705, 0.00802624),
                doubleArrayOf(-0.01248839, 0.01468714, -0.02547606),
                doubleArrayOf(0.01463082, 0.01595462, -0.03638381),
                doubleArrayOf(0.01133139, 0.00441169, -0.00368798),
                doubleArrayOf(-0.00713461, -0.01849457, -0.00297135),
                doubleArrayOf(-0.01224275, -0.00545467, -0.03361925),
                doubleArrayOf(0.01435493, -0.00646228, -0.03768723),
                doubleArrayOf(0.0162458, -0.01513728, -0.01682711),
                doubleArrayOf(0.0006955, -0.03268304, -0.01524339),
                doubleArrayOf(-0.01213821, -0.02572323, -0.03215103),
                doubleArrayOf(0.00354057, -0.02205115, -0.04311233),
                doubleArrayOf(0.01097236, -0.02941061, -0.03285922)
        )

        val targetGesture3dMatrix: RealMatrix = MatrixUtils.createRealMatrix(targetGesture3dData)
        fun normalize(X: RealMatrix, origin: RealMatrix? = null, scale: Double? = null): RealMatrix {
            val originActual = origin ?: meanRow(X)
            var normalizedX = X.subtract(stack(originActual,X.rowDimension))

            // Scaling
            val scaleActual = if (scale != null) {
                MatrixUtils.createRowRealMatrix(DoubleArray(X.columnDimension) { scale })
            } else {
                MatrixUtils.createRowRealMatrix(DoubleArray(X.columnDimension) { i -> normalizedX.getColumn(i).standardDeviation() })
            }

            for (i in 0 until normalizedX.rowDimension) {
                for (j in 0 until normalizedX.columnDimension) {
                    normalizedX.setEntry(i, j, normalizedX.getEntry(i, j) / scaleActual.getEntry(0, j))
                }
            }

            return normalizedX
        }

        fun DoubleArray.standardDeviation(): Double {
            val mean = this.average()
            return kotlin.math.sqrt(this.map { it - mean }.map { it * it }.average())
        }

        fun procrustes(X: RealMatrix, Y: RealMatrix): Pair<RealMatrix, RealMatrix> {
            val xCentroid = meanRow(X)
            val yCentroid = meanRow(Y)

            val xCentered = X.subtract(stack(xCentroid, X.rowDimension))
            val yCentered = Y.subtract(stack(yCentroid, Y.rowDimension))

            val A = xCentered.transpose().multiply(yCentered)
            val svd = SingularValueDecomposition(A)
            val U = svd.u
            val VT = svd.vt

            var R = U.multiply(VT)
            if (EigenDecomposition(R).determinant < 0) {
                VT.setRowMatrix(VT.rowDimension - 1, VT.getRowMatrix(VT.rowDimension - 1).scalarMultiply(-1.0))
                R = U.multiply(VT)
            }

            val yTransformed = yCentered.multiply(R.transpose()).add(stack(xCentroid, yCentered.rowDimension))
            return Pair(R, yTransformed)
        }

        fun euclideanDistance(X: RealMatrix, Y: RealMatrix): Double {
            val difference = X.subtract(Y)
            var sum = 0.0
            for (i in 0 until difference.rowDimension) {
                var rowSum = 0.0
                for (j in 0 until difference.columnDimension) {
                    rowSum += difference.getEntry(i, j).pow(2)
                }
                sum += sqrt(rowSum)
            }
            return sum / difference.rowDimension
        }

        fun euclideanSimilarity(meanEuclideanDistance: Double, originalDataStandardDeviation: Double = 1.0): Double {
            val meanDistanceIfIidFrom3dGaussian = 4 / sqrt(Math.PI) // if two 3D points are sampled from a 3D Standard Gaussian, the expected distance between them is 4/sqrt(pi).
            val adjustedMeanDistance = meanDistanceIfIidFrom3dGaussian * originalDataStandardDeviation
            val similarity = 1 - meanEuclideanDistance / adjustedMeanDistance
            return similarity
        }

        fun gestureSimilarity(currentPosition: RealMatrix, targetPosition: RealMatrix=targetGesture3dMatrix, rotationInvariant: Boolean = true, mirrorInvariant: Boolean = true): Double {
            if (mirrorInvariant) {
                val reversedCurrentPosition = reverseColumns(currentPosition)
                return gestureSimilarity(reversedCurrentPosition, targetPosition, rotationInvariant, false).coerceAtLeast(gestureSimilarity(currentPosition, targetPosition, rotationInvariant, false))
            }
            val xCentered = targetPosition.subtract(stack(meanRow(targetPosition), targetPosition.rowDimension))
            val yCentered = currentPosition.subtract(stack(meanRow(currentPosition), currentPosition.rowDimension))
            val xAvgDistance = xCentered.normRows().average()
            val yAvgDistance = yCentered.normRows().average()

            val xNormalized = normalize(targetPosition, origin = targetPosition.getRowMatrix(0), scale = xAvgDistance)
            val yNormalized = normalize(currentPosition, origin = currentPosition.getRowMatrix(0), scale = yAvgDistance)

            val meanEuclideanDistance = if (rotationInvariant) {
                val (_, yTransformed) = procrustes(xNormalized, yNormalized)
                val (_, xTransformed) = procrustes(yNormalized, xNormalized)
                (euclideanDistance(normalize(xNormalized), normalize(yTransformed)) + euclideanDistance(normalize(xTransformed), normalize(yNormalized))) / 2
            } else {
                euclideanDistance(normalize(xNormalized), normalize(yNormalized))
            }
            return euclideanSimilarity(meanEuclideanDistance)
        }


        fun meanRow(matrix: RealMatrix): RealMatrix {
            return MatrixUtils.createRowRealMatrix(DoubleArray(matrix.columnDimension) { i -> matrix.getColumn(i).average() })
        }

        fun RealMatrix.normRows(): DoubleArray {
            return DoubleArray(this.rowDimension) { i -> sqrt(this.getRow(i).sumOf { it * it }) }
        }

        fun stack(rowMatrix: RealMatrix, n: Int): RealMatrix {
            val rows = rowMatrix.rowDimension
            val cols = rowMatrix.columnDimension
            require(rows == 1) { "Input must be a row matrix (1xm)" }

            // Create a new matrix with n times the rows of the original matrix
            val stackedMatrix = MatrixUtils.createRealMatrix(n, cols)

            // Fill the new matrix with copies of the original row
            for (i in 0 until n) {
                stackedMatrix.setRowMatrix(i, rowMatrix)
            }
            return stackedMatrix
        }

        fun reverseColumns(matrix: RealMatrix): RealMatrix {
            val rows = matrix.rowDimension
            val cols = matrix.columnDimension
            val reversed = MatrixUtils.createRealMatrix(rows, cols)
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    reversed.setEntry(i, j, matrix.getEntry(i, cols - j - 1))
                }
            }
            return reversed
        }

        fun normalVector(X: RealMatrix): DoubleArray {
            val n = X.rowDimension

            // Compute the centroid
            val centroid = meanRow(X)

            val xCentered = X.subtract(stack(centroid, X.rowDimension))

            // Compute the covariance matrix
            val C = xCentered.transpose().multiply(xCentered).scalarMultiply(1.0 / n)

            // Compute the eigenvectors and eigenvalues
            val eig = EigenDecomposition(C)

            // Find the eigenvector corresponding to the smallest eigenvalue
            val indexOfMinEigenvalue = eig.realEigenvalues.indices.minBy { eig.realEigenvalues[it] } ?: 0
            val normalVector = eig.getEigenvector(indexOfMinEigenvalue).toArray()

            return normalVector
        }
    }
}


