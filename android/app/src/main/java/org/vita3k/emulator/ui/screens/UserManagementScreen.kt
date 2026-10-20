package org.vita3k.emulator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.vita3k.emulator.R
import org.vita3k.emulator.data.NativeUser
import org.vita3k.emulator.ui.components.AppButton
import org.vita3k.emulator.ui.components.AppDialog
import org.vita3k.emulator.ui.components.ButtonTone
import org.vita3k.emulator.ui.components.DialogAction
import org.vita3k.emulator.ui.components.EmptyState
import org.vita3k.emulator.ui.components.EyebrowLabel
import org.vita3k.emulator.ui.components.Tag
import org.vita3k.emulator.ui.components.ToolbarButton
import org.vita3k.emulator.ui.theme.Mocha
import org.vita3k.emulator.ui.theme.MonoSmall
import org.vita3k.emulator.ui.viewmodel.UserManagementViewModel

private val AvatarTints = listOf(Mocha.Mauve, Mocha.Blue, Mocha.Teal, Mocha.Peach, Mocha.Pink, Mocha.Green, Mocha.Yellow, Mocha.Sapphire)

private fun NativeUser.tint(): Color = AvatarTints[(id.hashCode() and Int.MAX_VALUE) % AvatarTints.size]

@Composable
fun UserManagementScreen(
    viewModel: UserManagementViewModel,
    onBack: () -> Unit,
    onUsersChanged: () -> Unit
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var pendingDeletion by remember { mutableStateOf<NativeUser?>(null) }
    val activeUser = viewModel.users.firstOrNull { it.active }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolbarButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back), onBack)
                Text(
                    stringResource(R.string.user_management_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Mocha.Text,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                )
                ToolbarButton(
                    Icons.Default.Refresh,
                    stringResource(R.string.user_management_refresh),
                    viewModel::load,
                    enabled = !viewModel.loading && !viewModel.busy
                )
            }

            when {
                viewModel.loading && viewModel.users.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Mocha.Mauve, trackColor = Mocha.Surface0)
                    }
                }
                viewModel.users.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Default.People,
                            title = stringResource(R.string.user_management_empty_title),
                            body = stringResource(R.string.user_management_empty_message)
                        ) {
                            AppButton(stringResource(R.string.user_management_create_user), { showCreateDialog = true }, icon = Icons.Default.Add)
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        LazyColumn(
                            modifier = Modifier
                                .widthIn(max = 640.dp)
                                .fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 112.dp)
                        ) {
                            item {
                                ActiveUserCard(activeUser)
                            }
                            item {
                                EyebrowLabel(
                                    stringResource(R.string.user_management_title),
                                    modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 10.dp)
                                )
                            }
                            itemsIndexed(viewModel.users, key = { _, user -> user.id }) { index, user ->
                                UserRow(
                                    user = user,
                                    busy = viewModel.busy,
                                    first = index == 0,
                                    last = index == viewModel.users.lastIndex,
                                    onActivate = { viewModel.activateUser(user, onUsersChanged) },
                                    onDelete = { pendingDeletion = user }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (viewModel.users.isNotEmpty()) {
            AppButton(
                text = stringResource(R.string.user_management_create_user),
                onClick = { showCreateDialog = true },
                icon = Icons.Default.Add,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(20.dp)
            )
        }
    }

    if (showCreateDialog) {
        CreateUserDialog(
            busy = viewModel.busy,
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                showCreateDialog = false
                viewModel.createUser(name, onUsersChanged)
            }
        )
    }

    pendingDeletion?.let { user ->
        AppDialog(
            title = stringResource(R.string.user_management_delete_title),
            onDismiss = { pendingDeletion = null },
            actions = listOf(
                DialogAction(stringResource(R.string.action_cancel), { pendingDeletion = null }),
                DialogAction(stringResource(R.string.action_delete), {
                    pendingDeletion = null
                    viewModel.deleteUser(user, onUsersChanged)
                }, ButtonTone.Danger)
            )
        ) {
            Text(stringResource(R.string.user_management_delete_message, user.name))
        }
    }

    viewModel.operationResult?.let { result ->
        AppDialog(
            title = if (result.isError) stringResource(R.string.settings_opt_error) else stringResource(R.string.settings_success_title),
            onDismiss = { viewModel.dismissOperationResult() },
            actions = listOf(DialogAction(stringResource(R.string.action_ok), { viewModel.dismissOperationResult() }, ButtonTone.Accent))
        ) {
            Text(result.message)
        }
    }
}

@Composable
private fun Avatar(name: String, tint: Color, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.18f))
            .border(1.dp, tint.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.trim().take(1).uppercase().ifEmpty { "?" },
            style = if (size >= 56) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            color = tint
        )
    }
}

@Composable
private fun ActiveUserCard(activeUser: NativeUser?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Mocha.Mantle)
            .border(1.dp, Mocha.Surface0, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Avatar(activeUser?.name ?: "?", activeUser?.tint() ?: Mocha.Overlay1, 60)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            EyebrowLabel(stringResource(R.string.user_management_active_user), color = Mocha.Mauve)
            Text(
                activeUser?.name ?: stringResource(R.string.user_management_active_none),
                style = MaterialTheme.typography.headlineSmall,
                color = Mocha.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            activeUser?.let { Text(it.id, style = MonoSmall, color = Mocha.Overlay1) }
        }
    }
}

@Composable
private fun UserRow(
    user: NativeUser,
    busy: Boolean,
    first: Boolean,
    last: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(
        topStart = if (first) 18.dp else 0.dp,
        topEnd = if (first) 18.dp else 0.dp,
        bottomStart = if (last) 18.dp else 0.dp,
        bottomEnd = if (last) 18.dp else 0.dp
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Mocha.Mantle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Avatar(user.name, user.tint(), 40)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(user.name, style = MaterialTheme.typography.titleSmall, color = Mocha.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(user.id, style = MonoSmall, color = Mocha.Overlay1)
            }
            if (user.active) {
                Tag(stringResource(R.string.user_management_active_badge), Mocha.Green)
            } else {
                AppButton(
                    text = stringResource(R.string.user_management_switch_user),
                    onClick = onActivate,
                    tone = ButtonTone.Neutral,
                    enabled = !busy
                )
            }
            ToolbarButton(
                Icons.Default.DeleteOutline,
                stringResource(R.string.user_management_delete_user),
                onDelete,
                enabled = !busy,
                tint = Mocha.Red
            )
        }
        if (!last) HorizontalDivider(color = Mocha.Surface0, modifier = Modifier.padding(start = 68.dp))
    }
}

@Composable
private fun CreateUserDialog(
    busy: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var userName by rememberSaveable { mutableStateOf("Vita3K") }

    AppDialog(
        title = stringResource(R.string.user_management_create_user),
        onDismiss = onDismiss,
        actions = listOf(
            DialogAction(stringResource(R.string.action_cancel), onDismiss),
            DialogAction(stringResource(R.string.action_ok), { onCreate(userName.trim()) }, ButtonTone.Accent, enabled = !busy && userName.trim().isNotEmpty())
        )
    ) {
        Text(stringResource(R.string.user_management_create_prompt))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            EyebrowLabel(stringResource(R.string.user_management_name_label))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Mocha.Crust)
                    .border(1.dp, Mocha.Mauve.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp)
            ) {
                BasicTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Mocha.Text),
                    cursorBrush = SolidColor(Mocha.Mauve),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
