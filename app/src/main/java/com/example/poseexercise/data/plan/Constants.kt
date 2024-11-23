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
                muscleTargetImage = R.drawable.push_up_muscle, // Add a muscle targeting diagram
                calorie = 3.2,
                level = "Intermediate",
                steps = listOf(
                    "Start in a plank position with hands shoulder-width apart",
                    "Lower your body until chest nearly touches the ground",
                    "Keep your core tight and body in a straight line",
                    "Push back up to starting position"
                ),
                targetMuscles = listOf("Chest", "Shoulders", "Triceps", "Core"),
                properFormGuide = listOf(
                    "Maintain a straight line from head to heels",
                    "Keep your core engaged throughout the movement",
                    "Position hands slightly wider than shoulder-width",
                    "Lower your body with control, not speed",
                    "Breathe in as you lower down, exhale as you push up"
                ),
                commonMistakesToAvoid = listOf(
                    "Dropping hips or arching lower back",
                    "Flaring elbows out at 90-degree angles",
                    "Rushing through repetitions",
                    "Not going low enough to engage full muscle range",
                    "Holding breath during the exercise"
                ),
                isGif = true,
            ),
            Exercise(
                id = 2,
                name = "Lunge",
                image = R.drawable.reverse_lunges,
                muscleTargetImage = R.drawable.lunge_muscle, // Add a muscle targeting diagram
                calorie = 3.0,
                level = "Beginner",
                steps = listOf(
                    "Stand with feet hip-width apart",
                    "Step backward with one leg",
                    "Lower your body until both knees are bent at 90 degrees",
                    "Push back through front heel to starting position"
                ),
                targetMuscles = listOf("Quadriceps", "Hamstrings", "Glutes", "Calves"),
                properFormGuide = listOf(
                    "Keep your chest upright and core engaged",
                    "Ensure your front knee stays aligned with your foot",
                    "Take a controlled step back and avoid rushing",
                    "Push through the heel of the front foot to return",
                    "Breathe steadily throughout"
                ),
                commonMistakesToAvoid = listOf(
                    "Allowing front knee to collapse inward",
                    "Taking too short or too long a step",
                    "Leaning too far forward or backward",
                    "Lifting the back heel off the ground",
                    "Holding breath during repetitions"
                ),
                isGif = true,
            ),
            Exercise(
                id = 3,
                name = "Squat",
                image = R.drawable.squat,
                muscleTargetImage = R.drawable.squat_muscle, // Add a muscle targeting diagram
                calorie = 3.8,
                level = "Beginner",
                steps = listOf(
                    "Stand with feet shoulder-width apart",
                    "Push hips back and bend knees",
                    "Lower until thighs are parallel to ground",
                    "Drive through heels to stand back up"
                ),
                targetMuscles = listOf("Quadriceps", "Hamstrings", "Glutes", "Core"),
                properFormGuide = listOf(
                    "Keep your chest up and back straight",
                    "Engage your core throughout the movement",
                    "Ensure knees track over your toes",
                    "Avoid lifting heels off the ground",
                    "Breathe in as you lower, exhale as you stand up"
                ),
                commonMistakesToAvoid = listOf(
                    "Allowing knees to collapse inward",
                    "Leaning too far forward",
                    "Rounding your back",
                    "Not lowering enough to parallel",
                    "Using momentum instead of controlled movement"
                ),
                isGif = true,
            ),
            Exercise(
                id = 4,
                name = "Sit up",
                image = R.drawable.sit_ups,
                muscleTargetImage = R.drawable.situp_muscle, // Add a muscle targeting diagram
                calorie = 5.0,
                level = "Advance",
                steps = listOf(
                    "Lie on your back with knees bent",
                    "Place hands behind your head",
                    "Lift your upper body off the ground",
                    "Lower back down with control"
                ),
                targetMuscles = listOf("Abdominals", "Hip Flexors", "Lower Back"),
                properFormGuide = listOf(
                    "Keep your feet flat on the ground and avoid lifting them",
                    "Engage your core muscles throughout the movement",
                    "Keep your movements controlled and avoid jerking",
                    "Exhale while lifting your upper body and inhale while lowering"
                ),
                commonMistakesToAvoid = listOf(
                    "Pulling your neck or head with your hands",
                    "Using momentum to lift your upper body",
                    "Arching your back excessively",
                    "Failing to engage your core muscles"
                ),
                isGif = true,
            ),
            Exercise(
                id = 5,
                name = "Chest press",
                image = R.drawable.chest_press,
                muscleTargetImage = R.drawable.chest_muscle, // Add a muscle targeting diagram
                calorie = 7.0,
                level = "Advance",
                steps = listOf(
                    "Lie on bench with feet flat on ground",
                    "Hold weights at chest level",
                    "Press weights up until arms are straight",
                    "Lower weights back to chest with control"
                ),
                targetMuscles = listOf("Chest", "Shoulders", "Triceps"),
                properFormGuide = listOf(
                    "Maintain a natural arch in your lower back",
                    "Keep wrists aligned with forearms",
                    "Lower weights slowly to control movement",
                    "Avoid locking elbows at the top",
                    "Breathe out as you press, breathe in as you lower"
                ),
                commonMistakesToAvoid = listOf(
                    "Overarching your lower back",
                    "Allowing wrists to bend",
                    "Rushing through the motion",
                    "Dropping weights too quickly",
                    "Failing to engage chest muscles"
                ),
                isGif = true,
            ),
            Exercise(
                id = 6,
                name = "Dead lift",
                image = R.drawable.dead_lift,
                muscleTargetImage = R.drawable.deadlift_muscle, // Add a muscle targeting diagram
                calorie = 10.0,
                level = "Advance",
                steps = listOf(
                    "Stand with feet hip-width apart",
                    "Hinge at hips to grab the bar",
                    "Keep back straight and chest up",
                    "Stand up by driving hips forward",
                    "Lower bar back down with control"
                ),
                targetMuscles = listOf("Lower Back", "Hamstrings", "Glutes", "Core"),
                properFormGuide = listOf(
                    "Keep your spine neutral and avoid rounding",
                    "Engage your core before lifting",
                    "Drive through your heels and not your toes",
                    "Lower the bar with control",
                    "Maintain a tight grip on the bar"
                ),
                commonMistakesToAvoid = listOf(
                    "Rounding the lower back",
                    "Pulling with arms instead of engaging legs",
                    "Jerking the bar off the ground",
                    "Letting the bar drift away from your body",
                    "Failing to lock hips at the top"
                ),
                isGif = true,
            ),
            Exercise(
                id = 7,
                name = "Shoulder press",
                image = R.drawable.shoulder_press,
                muscleTargetImage = R.drawable.shoulder_muscle, // Add a muscle targeting diagram
                calorie = 9.0,
                level = "Advance",
                steps = listOf(
                    "Hold weights at shoulder level",
                    "Keep core tight and back straight",
                    "Press weights overhead until arms lock",
                    "Lower weights back to shoulders"
                ),
                targetMuscles = listOf("Shoulders", "Triceps", "Upper Back"),
                properFormGuide = listOf(
                    "Keep your back straight and avoid arching",
                    "Engage your core throughout the movement",
                    "Do not lock your elbows forcefully at the top",
                    "Lower the weights slowly with control"
                ),
                commonMistakesToAvoid = listOf(
                    "Arching the lower back excessively",
                    "Using momentum instead of controlled movements",
                    "Letting the elbows flare out too much",
                    "Failing to engage the core"
                ),
                isGif = true,
            ),
            Exercise(
                id = 8,
                name = "Jumping jacks",
                image = R.drawable.jumping_jacks,
                muscleTargetImage = R.drawable.jacks_muscle, // Add a muscle targeting diagram
                calorie = 9.0,
                level = "Beginner",
                steps = listOf(
                    "Stand with feet together, arms at sides",
                    "Jump and spread legs while raising arms",
                    "Jump back to starting position",
                    "Maintain a steady rhythm"
                ),
                targetMuscles = listOf("Full Body", "Shoulders", "Calves", "Core"),
                properFormGuide = listOf(
                    "Land softly on your feet to reduce impact",
                    "Keep arms and legs fully extended during movement",
                    "Maintain a steady and rhythmic pace",
                    "Engage your core for better balance"
                ),
                commonMistakesToAvoid = listOf(
                    "Landing heavily on your feet",
                    "Failing to fully extend arms or legs",
                    "Losing rhythm and balance",
                    "Leaning forward excessively"
                ),
                isGif = true,
            ),
            Exercise(
                id = 9,
                name = "Planking",
                image = R.drawable.planking,
                muscleTargetImage = R.drawable.plank_muscle, // Add a muscle targeting diagram
                calorie = 9.0,
                level = "Intermediate",
                steps = listOf(
                    "Place forearms on ground, elbows under shoulders",
                    "Extend legs behind you",
                    "Keep body in straight line from head to heels",
                    "Hold position while breathing steadily"
                ),
                targetMuscles = listOf("Core", "Shoulders", "Back", "Glutes"),
                properFormGuide = listOf(
                    "Ensure your body forms a straight line",
                    "Engage your glutes and core for stability",
                    "Keep your neck neutral to avoid strain",
                    "Breathe steadily throughout the hold"
                ),
                commonMistakesToAvoid = listOf(
                    "Letting hips sag or rise too high",
                    "Holding your breath",
                    "Failing to engage the core and glutes",
                    "Straining the neck by looking forward"
                ),
                isGif = true,
            )
        )
    }
}