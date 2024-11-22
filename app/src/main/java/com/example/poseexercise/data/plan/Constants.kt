package com.example.poseexercise.data.plan

import com.example.poseexercise.R

/**
Object containing constants related to exercise data
 **/
object Constants {

    // Function to get a list of predefined exercises with their details
    fun getExerciseList(): List<Exercise> {
        return listOf(
            Exercise(
                id = 1,
                name = "Push up",
                image = R.drawable.push_up,
                calorie = 3.2,
                level = "Intermediate",
                steps = listOf(
                    "Start in a plank position with hands shoulder-width apart",
                    "Lower your body until chest nearly touches the ground",
                    "Keep your core tight and body in a straight line",
                    "Push back up to starting position"
                ),
                targetMuscles = listOf("Chest", "Shoulders", "Triceps", "Core")
            ),
            Exercise(
                id = 2,
                name = "Lunge",
                image = R.drawable.reverse_lunges,
                calorie = 3.0,
                level = "Beginner",
                steps = listOf(
                    "Stand with feet hip-width apart",
                    "Step backward with one leg",
                    "Lower your body until both knees are bent at 90 degrees",
                    "Push back through front heel to starting position"
                ),
                targetMuscles = listOf("Quadriceps", "Hamstrings", "Glutes", "Calves")
            ),
            Exercise(
                id = 3,
                name = "Squat",
                image = R.drawable.squat,
                calorie = 3.8,
                level = "Beginner",
                steps = listOf(
                    "Stand with feet shoulder-width apart",
                    "Push hips back and bend knees",
                    "Lower until thighs are parallel to ground",
                    "Drive through heels to stand back up"
                ),
                targetMuscles = listOf("Quadriceps", "Hamstrings", "Glutes", "Core")
            ),
//            Exercise(
//                id = 4,
//                name = "Sit up",
//                image = R.drawable.sit_ups,
//                calorie = 5.0,
//                level = "Advance",
//                steps = listOf(
//                    "Lie on your back with knees bent",
//                    "Place hands behind your head",
//                    "Lift your upper body off the ground",
//                    "Lower back down with control"
//                ),
//                targetMuscles = listOf("Abdominals", "Hip Flexors", "Lower Back")
//            ),
            Exercise(
                id = 5,
                name = "Chest press",
                image = R.drawable.chest_press,
                calorie = 7.0,
                level = "Advance",
                steps = listOf(
                    "Lie on bench with feet flat on ground",
                    "Hold weights at chest level",
                    "Press weights up until arms are straight",
                    "Lower weights back to chest with control"
                ),
                targetMuscles = listOf("Chest", "Shoulders", "Triceps")
            ),
            Exercise(
                id = 6,
                name = "Dead lift",
                image = R.drawable.dead_lift,
                calorie = 10.0,
                level = "Advance",
                steps = listOf(
                    "Stand with feet hip-width apart",
                    "Hinge at hips to grab the bar",
                    "Keep back straight and chest up",
                    "Stand up by driving hips forward",
                    "Lower bar back down with control"
                ),
                targetMuscles = listOf("Lower Back", "Hamstrings", "Glutes", "Core")
            ),
            Exercise(
                id = 7,
                name = "Shoulder press",
                image = R.drawable.shoulder_press,
                calorie = 9.0,
                level = "Advance",
                steps = listOf(
                    "Hold weights at shoulder level",
                    "Keep core tight and back straight",
                    "Press weights overhead until arms lock",
                    "Lower weights back to shoulders"
                ),
                targetMuscles = listOf("Shoulders", "Triceps", "Upper Back")
            ),
            Exercise(
                id = 8,
                name = "Jumping jacks",
                image = R.drawable.jumping_jacks,
                calorie = 9.0,
                level = "Beginner",
                steps = listOf(
                    "Stand with feet together, arms at sides",
                    "Jump and spread legs while raising arms",
                    "Jump back to starting position",
                    "Maintain a steady rhythm"
                ),
                targetMuscles = listOf("Full Body", "Shoulders", "Calves", "Core")
            ),
            Exercise(
                id = 9,
                name = "Planking",
                image = R.drawable.planking,
                calorie = 9.0,
                level = "Intermediate",
                steps = listOf(
                    "Place forearms on ground, elbows under shoulders",
                    "Extend legs behind you",
                    "Keep body in straight line from head to heels",
                    "Hold position while breathing steadily"
                ),
                targetMuscles = listOf("Core", "Shoulders", "Back", "Glutes")
            )
        )
    }
}