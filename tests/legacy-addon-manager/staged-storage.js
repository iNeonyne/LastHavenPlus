/**
 * Commits a new add-on storage generation without sacrificing the last committed one.
 * The metadata pointer is switched only after staged files are durable. If metadata
 * persistence fails, best-effort rollback removes only the unreferenced staged generation.
 */
export async function commitStagedGeneration({ stage, commit, rollback, cleanup }) {
  await stage();
  try {
    await commit();
  } catch (error) {
    await Promise.allSettled(rollback());
    throw error;
  }
  await cleanup();
}
