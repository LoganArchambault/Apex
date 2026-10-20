package com.example.apex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apex.R
import com.example.apex.data.EnumTypeActivite
import com.example.apex.data.Workout
import com.example.apex.ui.theme.ApexTheme


@Composable
fun WorkoutCard(
    workout: Workout,
    onFavoriToggle: () -> Unit = {},
    onCardClick: (Int) -> Unit = {}
) {
    WorkoutCard(
        id = workout.id,
        titre = workout.titre,
        typeActivite = workout.typeActivite,
        lieu = workout.lieu,
        isOutside = workout.isOutside,
        intensite = workout.intensite,
        notes = workout.notes,
        done = workout.done,
        favori = workout.favori,
        onFavoriToggle = onFavoriToggle,
        onCardClick = onCardClick
    )
}

@Composable
fun WorkoutCard(
    id: Int = 0,
    titre: String,
    typeActivite: EnumTypeActivite,
    lieu: String,
    isOutside: Boolean,
    intensite: Int,
    notes: String,
    done: Boolean,
    favori: Boolean = false,
    onFavoriToggle: () -> Unit = {},
    onCardClick: (Int) -> Unit = {}
) {
    Card(
        onClick = { onCardClick(id) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            tonalElevation = 2.dp,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = nomTypeActiviteCard(typeActivite),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = lieu,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                BtnFavori(
                    favori = favori,
                    onFavClick = onFavoriToggle
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.intensite, intensite),
                    style = MaterialTheme.typography.bodySmall
                )

                LinearProgressIndicator(
                    progress = intensite.coerceIn(1, 10) / 10f,
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                )

                if (done) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.cd_fait)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isOutside) {
                    stringResource(R.string.txt_ext)
                } else {
                    stringResource(R.string.txt_int)
                },
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun nomTypeActiviteCard(type: EnumTypeActivite): String {
    return when (type) {
        EnumTypeActivite.Course -> stringResource(R.string.activite_course)
        EnumTypeActivite.Velo -> stringResource(R.string.activite_velo)
        EnumTypeActivite.Natation -> stringResource(R.string.activite_natation)
        EnumTypeActivite.Musculation -> stringResource(R.string.activite_musculation)
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutCardPreview() {
    ApexTheme {
        WorkoutCard(
            id = 1,
            titre = "Séance de course",
            typeActivite = EnumTypeActivite.Course,
            lieu = "Parc",
            isOutside = true,
            intensite = 7,
            notes = "Bonne séance",
            done = true
        )
    }
}
